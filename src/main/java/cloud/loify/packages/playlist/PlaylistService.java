package cloud.loify.packages.playlist;

import cloud.loify.dto.AddTracksRequestDTO;
import cloud.loify.dto.CreatePlaylistRequestDTO;
import cloud.loify.dto.CreatePlaylistResponseDTO;
import cloud.loify.dto.response.PlaylistResponseDTO;
import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.dto.track.TrackItemDTO;
import cloud.loify.dto.track.TrackItemObjectDTO;
import cloud.loify.dto.track.TracksDTO;
import cloud.loify.packages.auth.AuthService;
import cloud.loify.packages.me.MeService;
import cloud.loify.packages.track.TrackService;
import cloud.loify.packages.utils.ImageUtils;
import cloud.loify.packages.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public Mono<PlaylistResponseDTO> getPlaylistById(String playlistId) {
        logger.info("Retrieving playlist details for ID: {}", playlistId);
        return this.auth.webClient.get()
                .uri("v1/playlists/" + playlistId)
                .retrieve()
                .bodyToMono(PlaylistResponseDTO.class)
                .doOnSuccess(playlist -> logger.info("Successfully retrieved playlist: {}", playlist))
                .doOnError(err -> logger.error("Error retrieving playlist ID {}: {}", playlistId, err.getMessage()));
    }

    // TODO: This MAY be able to be removed - does it return BASICALLY the same content as `getPlaylistById()`
    public Mono<TracksDTO> getAllTracksInPlaylist(String playlistId) {
        logger.info("Retrieving all tracks for playlist ID: {}", playlistId);
        return this.auth.webClient.get()
                .uri("/v1/playlists/" + playlistId + "/tracks")
                .retrieve()
                .bodyToMono(TracksDTO.class)
                .doOnSuccess(tracks -> logger.info("Successfully retrieved tracks for playlist ID: {}", playlistId))
                .doOnError(err -> logger.error("Error retrieving tracks for playlist ID {}: {}", playlistId, err.getMessage()));
    }

    // TODO: Fix - "spotify:track:54eCPwH8hZqAJBMlZ9YEyJ" --> "54eCPwH8hZqAJBMlZ9YEyJ" (if deemed possible)
    public Mono<String> addTracksToPlaylist(String playlistId, AddTracksRequestDTO requestBody) {
        logger.info("Adding tracks to playlist ID: {} with request body: {}", playlistId, requestBody);
        return this.auth.webClient.post()
                .uri("v1/playlists/" + playlistId + "/tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)  // NOTE: String = `snapshot_id`
                .doOnSuccess(snapshotId -> logger.info("Successfully added tracks to playlist ID: {}. Snapshot ID: {}", playlistId, snapshotId))
                .doOnError(err -> logger.error("Error adding tracks to playlist ID {}: {}", playlistId, err.getMessage()));
    }

    public Flux<TrackSearchResponseDTO> getAndLoifyAllTracksInPlaylist(String playlistId) {
        // STEP 1: Get all `tracks` in playlist --- (maybe just call `getAllTracksInPlaylist()`)
        logger.info("Getting all tracks in playlist ID: {}", playlistId);
        TracksDTO tracks = this.getAllTracksInPlaylist(playlistId).block();

        // STEP 2: Extract all `tracksNames` in playlist
        Stream<String> trackNames = tracks.items()
                .stream()
                .map((t) -> (TrackItemDTO) t)
                .map((t) -> t.track().name());

        // STEP 3: "loify" all `trackNames` - `loifyedTrackNames = trackNames.map(this::loifyTrackName)`
        Stream<String> loifyedTrackNames = trackNames.map(StringUtils::loifyTrackName);

        // STEP 3: Get all "loify-ed" tracks (by using FIRST_TRACK algo) - `loifedTracks = loifyedTrackNames.map(this::getFirstTrackByTrackName)`
        Flux<TrackSearchResponseDTO> loifyedTracks = Flux.fromStream(loifyedTrackNames
                        .map(this.track::getFirstTrackByTrackName))
                .flatMap(mono -> mono);  // Flatten the Mono<Track> into a Flux<Track>

        // STEP 4: Now we have all Mono<TrackDTO> `loifyedTracks`, simply return - `return loifyedTracks`
        logger.info("Successfully loified all tracks in playlist ID: {}", playlistId);
        return loifyedTracks;
    }

    // TODO: Make this atomic - because sometimes the playlist is created, but the songs aren't added
    // TODO: ^ need to make it so that if one fails, then it's as if the method was never called
    // TODO: ^ (might involve deleting the playlist if the transaction fails)
    public CreatePlaylistResponseDTO createLoifyedPlaylistAndAddLoifyedTracks(String playlistId) {
        // STEP 0: Get current playlist details
        logger.info("Creating loified playlist and adding loified tracks for playlist ID: {}", playlistId);
        PlaylistResponseDTO currentPlaylist = this.getPlaylistById(playlistId).block();
        String currentPlaylistName = currentPlaylist.name();
        String loifyPlaylistName = StringUtils.loifyPlaylistName(currentPlaylistName);
        String loifyPlaylistDescription = StringUtils.loifyPlaylistDescription(currentPlaylistName);

        // TODO: Check if playlist exists already
        CreatePlaylistRequestDTO createPlaylistReqBody = new CreatePlaylistRequestDTO(loifyPlaylistName, loifyPlaylistDescription, true, true);

        // TODO: Replace`this.createPlaylist("""""xyz"""")` with an actual way to get `this.userProfile.id()
        CreatePlaylistResponseDTO response = this.me.createPlaylistForCurrentUser(createPlaylistReqBody).block();
        String loifyPlaylistId = response.id();

        // STEP 2: Update (empty 🍃) playlist - image
        try {
            String currentPlaylistImage = currentPlaylist.images().get(0).url();
            String loifyPlaylistImage = ImageUtils.loifyPlaylistImage(currentPlaylistImage);
            this.updatePlaylistImage(loifyPlaylistId, loifyPlaylistImage).block();
        }
        catch (IOException e) {
            logger.error("Error while processing the playlist image: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            // TODO: Implement default cover image
            logger.warn("Original Playlist does not have cover image... fallback to default loify image");
        }

        // STEP 3: Loify all tracks in playlist -> return an array of 🍃tracks -> Flux<TrackSearchResponseDTO>.block()
        List<TrackSearchResponseDTO> loifyedTracks = this.getAndLoifyAllTracksInPlaylist(playlistId).collectList().block();
        logger.info("Loified tracks for playlist ID: {}", playlistId);

        // STEP 4: Add loify-ed tracks -> to (empty 🍃) playlist
        List<String> uris = loifyedTracks
                .stream()
                .map((t) -> {
                    try {
                        return (TrackItemObjectDTO) t.tracks().items().get(0);
                    } catch (Exception e) {
                        logger.warn("No track found - skipping item...");
                        return null;
                    }
                })
                .map((t) -> {
                    try {
                        return "spotify:track:" + t.id();   // TODO: Add `uri` field to DTO and use that instead of appending here?
                    } catch (Exception e) {
                        logger.warn("No track found - skipping item again...");
                        return null;
                    }
                })
                .filter(t -> t != null)
                .collect(Collectors.toList());

        AddTracksRequestDTO addTracksReqBody = new AddTracksRequestDTO(uris);

        logger.info("Adding loified tracks to new playlist ID: {}", loifyPlaylistId);
        this.addTracksToPlaylist(loifyPlaylistId, addTracksReqBody).block();       // <- TODO: make the playlist dynamic (must come from above )

        // STEP 5: Return the `href` url of the 🍃 playlist - so that users can view in Spotify
        return response;
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
}
