package cloud.loify.packages.playlist;

import cloud.loify.packages.playlist.dto.CreatePlaylistRequestDTO;
import cloud.loify.packages.playlist.dto.CreatePlaylistResponseDTO;
import cloud.loify.packages.playlist.dto.AddTracksToPlaylistRequestDTO;
import cloud.loify.packages.playlist.dto.GetPlaylistResponseDTO;
import cloud.loify.packages.track.dto.SearchTrackResponseDTO;
import cloud.loify.packages.track.dto.GetTracksFromPlaylistResponseDTO;
import cloud.loify.packages.me.MeService;
import cloud.loify.packages.track.TrackService;
import cloud.loify.packages.utils.ImageUtils;
import cloud.loify.packages.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import java.io.IOException;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private final WebClient webClient;
    private final TrackService track;
    private final MeService me;

    // Logger instance
    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    // TODO: should TrackService be injected here? or should the method required (getFirstTrackByTrackName) be made static?
    public PlaylistService(MeService meService, TrackService trackService, WebClient webClient) {
        this.me = meService;
        this.track = trackService;
        this.webClient = webClient;
    }

    public Mono<GetPlaylistResponseDTO> getPlaylistById(String playlistId) {
        logger.info("Retrieving playlist details for ID: {}", playlistId);
        return this.webClient.get()
                .uri("/playlists/" + playlistId)
                .retrieve()
                .bodyToMono(GetPlaylistResponseDTO.class)
                .doOnSuccess(playlist -> logger.info("Successfully retrieved playlist: {}", playlist))
                .doOnError(err -> logger.error("Error retrieving playlist ID {}: {}", playlistId, err.getMessage()));
    }

    // TODO: This MAY be able to be removed - does it return BASICALLY the same content as `getPlaylistById()`
    public Mono<GetTracksFromPlaylistResponseDTO> getAllTracksInPlaylist(String playlistId) {
        logger.info("Retrieving all tracks for playlist ID: {}", playlistId);
        return this.webClient.get()
                .uri("/playlists/" + playlistId + "/tracks")
                .retrieve()
                .bodyToMono(GetTracksFromPlaylistResponseDTO.class)
                .doOnSuccess(tracks -> logger.info("Successfully retrieved tracks for playlist ID: {}", playlistId))
                .doOnError(err -> logger.error("Error retrieving tracks for playlist ID {}: {}", playlistId, err.getMessage()));
    }

    // TODO: Fix - "spotify:track:54eCPwH8hZqAJBMlZ9YEyJ" --> "54eCPwH8hZqAJBMlZ9YEyJ" (if deemed possible)
    public Mono<String> addTracksToPlaylist(String playlistId, AddTracksToPlaylistRequestDTO requestBody) {
        logger.info("Adding tracks to playlist ID: {} with request body: {}", playlistId, requestBody);
        return this.webClient.post()
                .uri("/playlists/" + playlistId + "/tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(snapshotId -> logger.info("Successfully added tracks to playlist ID: {}. Snapshot ID: {}", playlistId, snapshotId))
                .doOnError(err -> logger.error("Error adding tracks to playlist ID [{}]: {}", playlistId, err.getMessage()));
    }

    public Flux<SearchTrackResponseDTO> getAndLoifyAllTracksInPlaylist(String playlistId, String genre) {
        logger.info("Getting all tracks in playlist ID: {}", playlistId);
        return this.getAllTracksInPlaylist(playlistId)
                .flatMapMany(tracks -> {
                    if (tracks == null || tracks.items() == null || tracks.items().isEmpty()) {
                        logger.warn("No tracks found in playlist ID: {}", playlistId);
                        return Flux.empty(); // Return an empty Flux if no tracks are found
                    }

                    return Flux.fromIterable(tracks.items())
                            .map(t -> StringUtils.customizeTrackName(t.track().name(), genre))
                            .flatMap(this.track::getFirstTrackByTrackName);
                })
                .doOnComplete(() -> logger.info("Successfully loifyed all tracks in playlist ID: {}", playlistId))
                .doOnError(err -> {
                    logger.error("Error loifying tracks in playlist ID {}: {}", playlistId, err.getMessage());
                    throw new RuntimeException(err);
                });
    }

    // TODO: Make this atomic - because sometimes the playlist is created, but the songs aren't added
    // TODO: ^ need to make it so that if one fails, then it's as if the method was never called
    // TODO: ^ (might involve deleting the playlist if the transaction fails)
    public Mono<CreatePlaylistResponseDTO> createLoifyedPlaylistAndAddLoifyedTracks(String playlistId, String genre) {
        // STEP 0: Get current playlist details
        logger.info("Creating loifyed playlist and adding loifyed tracks for playlist ID: {}", playlistId);

        return this.getPlaylistById(playlistId)  // Get the current playlist details reactively
                .flatMap(currentPlaylist -> {
                    String currentPlaylistName = currentPlaylist.name();
                    String loifyPlaylistName = StringUtils.customizePlaylistName(currentPlaylistName, genre);
                    String loifyPlaylistDescription = StringUtils.customizePlaylistDescription(currentPlaylistName, genre);

                    // Create the request body for the new playlist
                    CreatePlaylistRequestDTO createPlaylistReqBody = new CreatePlaylistRequestDTO(loifyPlaylistName, loifyPlaylistDescription, true, true);

                    // STEP 1: Create the new playlist
                    return this.me.createPlaylistForCurrentUser(createPlaylistReqBody)
                            .flatMap(response -> {
                                String loifyPlaylistId = response.id();

                                // STEP 2: Update the playlist image
                                String currentPlaylistImage = currentPlaylist.image().url();
                                String loifyPlaylistImage = null;
                                try {
                                    loifyPlaylistImage = ImageUtils.loifyPlaylistImage(currentPlaylistImage);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                return this.updatePlaylistImage(loifyPlaylistId, loifyPlaylistImage)

                                        .then(this.getAndLoifyAllTracksInPlaylist(playlistId, genre) // STEP 3: Get loifyed tracks
                                                .collectList() // Collect all loifyed tracks into a List
                                                .flatMap(loifyedTracks -> {
                                                    // STEP 4: Prepare the list of track URIs
                                                    List<String> uris = loifyedTracks.stream()
                                                            .map(t -> {
                                                                try {
                                                                    return t.tracks().items().get(0);
                                                                } catch (Exception e) {
                                                                    logger.warn("No track found - skipping item...");
                                                                    return null;
                                                                }
                                                            })
                                                            .map(t -> {
                                                                try {
                                                                    return "spotify:track:" + t.id(); // Construct URI
                                                                } catch (Exception e) {
                                                                    logger.warn("No track found - skipping item again...");
                                                                    return null;
                                                                }
                                                            })
                                                            .filter(t -> t != null)
                                                            .collect(Collectors.toList());

                                                    // Prepare the request body for adding tracks
                                                    AddTracksToPlaylistRequestDTO addTracksReqBody = new AddTracksToPlaylistRequestDTO(uris);
                                                    logger.info("Adding loifyed tracks to new playlist ID: {}", loifyPlaylistId);

                                                    // Add loifyed tracks to the new playlist
                                                    return this.addTracksToPlaylist(loifyPlaylistId, addTracksReqBody).then(Mono.just(response)); // Return the response after adding tracks
                                                }));
                            });
                });
    }

    private Mono<String> updatePlaylistImage(String playlistId, String base64Image) {
        logger.info("Updating playlist image for playlist ID: {}", playlistId);

        return this.webClient.put()
                .uri("/playlists/" + playlistId + "/images")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(base64Image)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(success -> logger.info("Successfully updated playlist image for playlist ID: {}", playlistId))
                .doOnError(err -> logger.error("Error updating playlist image for playlist ID {}: {}", playlistId, err.getMessage()))
                .onErrorResume(err -> {
                    logger.warn("Error while updating playlist image: {}. Falling back to default image.", err.getMessage());
                    return this.updatePlaylistImage(playlistId, ImageUtils.DEFAULT_BASE_IMAGE_64);
                });
    }
}
