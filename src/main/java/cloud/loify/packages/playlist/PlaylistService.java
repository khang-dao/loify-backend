package cloud.loify.packages.playlist;

import cloud.loify.packages.playlist.dto.CreatePlaylistRequestDTO;
import cloud.loify.packages.playlist.dto.CreatePlaylistResponseDTO;
import cloud.loify.packages.playlist.dto.AddTracksToPlaylistRequestDTO;
import cloud.loify.packages.playlist.dto.GetPlaylistResponseDTO;
import cloud.loify.packages.track.dto.SearchTrackResponseDTO;
import cloud.loify.packages.track.dto.TrackDetailsFromPlaylistDTO;
import cloud.loify.packages.track.dto.GetTracksFromPlaylistResponseDTO;
import cloud.loify.packages.auth.AuthService;
import cloud.loify.packages.me.MeService;
import cloud.loify.packages.track.TrackService;
import cloud.loify.packages.utils.ImageUtils;
import cloud.loify.packages.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

import java.io.IOException;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private final AuthService auth;
    private final TrackService track;
    private final MeService me;

    // Logger instance
    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    // TODO: should TrackService be injected here? or should the method required (getFirstTrackByTrackName) be made static?
    public PlaylistService(AuthService authService, TrackService trackService, MeService meService) {
        this.auth = authService;
        this.track = trackService;
        this.me = meService;
    }

    public Mono<GetPlaylistResponseDTO> getPlaylistById(String playlistId) {
        logger.info("Retrieving playlist details for ID: {}", playlistId);
        return this.auth.webClient.get()
                .uri("v1/playlists/" + playlistId)
                .retrieve()
                .bodyToMono(GetPlaylistResponseDTO.class)
                .doOnSuccess(playlist -> logger.info("Successfully retrieved playlist: {}", playlist))
                .doOnError(err -> logger.error("Error retrieving playlist ID {}: {}", playlistId, err.getMessage()));
    }

    // TODO: This MAY be able to be removed - does it return BASICALLY the same content as `getPlaylistById()`
    public Mono<GetTracksFromPlaylistResponseDTO> getAllTracksInPlaylist(String playlistId) {
        logger.info("Retrieving all tracks for playlist ID: {}", playlistId);
        return this.auth.webClient.get()
                .uri("/v1/playlists/" + playlistId + "/tracks")
                .retrieve()
                .bodyToMono(GetTracksFromPlaylistResponseDTO.class)
                .doOnSuccess(tracks -> logger.info("Successfully retrieved tracks for playlist ID: {}", playlistId))
                .doOnError(err -> logger.error("Error retrieving tracks for playlist ID {}: {}", playlistId, err.getMessage()));
    }

    // TODO: Fix - "spotify:track:54eCPwH8hZqAJBMlZ9YEyJ" --> "54eCPwH8hZqAJBMlZ9YEyJ" (if deemed possible)
    public Mono<String> addTracksToPlaylist(String playlistId, AddTracksToPlaylistRequestDTO requestBody) {
        logger.info("Adding tracks to playlist ID: {} with request body: {}", playlistId, requestBody);
        return this.auth.webClient.post()
                .uri("v1/playlists/" + playlistId + "/tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)  // NOTE: String = `snapshot_id`
                .doOnSuccess(snapshotId -> logger.info("Successfully added tracks to playlist ID: {}. Snapshot ID: {}", playlistId, snapshotId))
                .doOnError(err -> logger.error("Error adding tracks to playlist ID [{}]: {}", playlistId, err.getMessage()));
    }

    public Flux<SearchTrackResponseDTO> getAndLoifyAllTracksInPlaylist(String playlistId) {
        logger.info("Getting all tracks in playlist ID: {}", playlistId);

        return this.getAllTracksInPlaylist(playlistId)
                .flatMapMany(tracks -> {
                    if (tracks == null || tracks.items() == null || tracks.items().isEmpty()) {
                        logger.warn("No tracks found in playlist ID: {}", playlistId);
                        return Flux.empty(); // Return an empty Flux if no tracks are found
                    }

                    return Flux.fromIterable(tracks.items())
                            .map(t -> (TrackDetailsFromPlaylistDTO) t) // TODO: might be able to delete
                            .map(t -> StringUtils.loifyTrackName(t.track().name()))
                            .flatMap(this.track::getFirstTrackByTrackName);
                })
                .doOnComplete(() -> logger.info("Successfully loified all tracks in playlist ID: {}", playlistId))
                .doOnError(err -> {
                    logger.error("Error loifying tracks in playlist ID {}: {}", playlistId, err.getMessage());
                    throw new RuntimeException(err);
                });
    }



    // TODO: Make this atomic - because sometimes the playlist is created, but the songs aren't added
    // TODO: ^ need to make it so that if one fails, then it's as if the method was never called
    // TODO: ^ (might involve deleting the playlist if the transaction fails)
    public Mono<CreatePlaylistResponseDTO> createLoifyedPlaylistAndAddLoifyedTracks(String playlistId) {
        // STEP 0: Get current playlist details
        logger.info("Creating loified playlist and adding loified tracks for playlist ID: {}", playlistId);

        return this.getPlaylistById(playlistId)  // Get the current playlist details reactively
                .flatMap(currentPlaylist -> {
                    String currentPlaylistName = currentPlaylist.name();
                    String loifyPlaylistName = StringUtils.loifyPlaylistName(currentPlaylistName);
                    String loifyPlaylistDescription = StringUtils.loifyPlaylistDescription(currentPlaylistName);

                    // Create the request body for the new playlist
                    CreatePlaylistRequestDTO createPlaylistReqBody = new CreatePlaylistRequestDTO(loifyPlaylistName, loifyPlaylistDescription, true, true);

                    // Create the new playlist
                    return this.me.createPlaylistForCurrentUser(createPlaylistReqBody)
                            .flatMap(response -> {
                                String loifyPlaylistId = response.id();

                                // STEP 2: Update the playlist image
                                String currentPlaylistImage = currentPlaylist.images().isEmpty() ? null : currentPlaylist.images().get(0).url();
                                String loifyPlaylistImage = null;
                                try {
                                    loifyPlaylistImage = ImageUtils.loifyPlaylistImage(currentPlaylistImage);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                return this.updatePlaylistImage(loifyPlaylistId, loifyPlaylistImage)
                                        .onErrorResume(err -> {
                                            logger.warn("Error while updating playlist image: {}. Falling back to default image.", err.getMessage());
                                            // Specify a default base64 image string here
                                            String defaultImageBase64 = "default_base64_image_string"; // Replace with the actual base64 string
                                            return this.updatePlaylistImage(loifyPlaylistId, defaultImageBase64);
                                        })
                                        .then(this.getAndLoifyAllTracksInPlaylist(playlistId) // STEP 3: Get loified tracks
                                                .collectList() // Collect all loified tracks into a List
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

                                                    // Add loified tracks to the new playlist
                                                    return this.addTracksToPlaylistWithRetry(loifyPlaylistId, addTracksReqBody)
                                                            .then(Mono.just(response)); // Return the response after adding tracks
                                                }));
                            });
                });
    }


    public Mono<String> updatePlaylistImage(String playlistId, String base64Image) {
        logger.info("Updating playlist image for playlist ID: {}", playlistId);
        return this.auth.webClient.put()
                .uri("v1/playlists/" + playlistId + "/images")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(base64Image)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(success -> logger.info("Successfully updated playlist image for playlist ID: {}", playlistId))
                .doOnError(err -> logger.error("Error updating playlist image for playlist ID {}: {}", playlistId, err.getMessage()));
    }


















    // TODO: Make this a function: GENERIC + UTIL
    // Retry logic for adding tracks with Retry-After support
    private Mono<Void> addTracksToPlaylistWithRetry(String playlistId, AddTracksToPlaylistRequestDTO requestBody) {
        return this.addTracksToPlaylist(playlistId, requestBody)
                .retryWhen(Retry.from(companion -> companion.handle((retrySignal, sink) -> {
                    Throwable failure = retrySignal.failure();
                    if (failure instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) failure;
                        if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            // Get the Retry-After header
                            String retryAfterHeader = ex.getHeaders().getFirst("Retry-After");
                            long retryAfterSeconds = retryAfterHeader != null ? Long.parseLong(retryAfterHeader) : 1;

                            logger.warn("Received 429 Too Many Requests. Retrying after {} seconds.", retryAfterSeconds);

                            // Delay before retrying
                            sink.next(Duration.ofSeconds(retryAfterSeconds));
                        } else {
                            sink.error(failure); // Stop retrying for other HTTP errors
                        }
                    } else {
                        sink.error(failure); // Stop retrying for non-HTTP errors
                    }
                }))).then();
    }
}
