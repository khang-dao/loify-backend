package cloud.loify.packages.playlist;

import cloud.loify.packages.playlist.dto.AddTracksToPlaylistRequestDTO;
import cloud.loify.packages.playlist.dto.CreatePlaylistResponseDTO;
import cloud.loify.packages.playlist.dto.GetPlaylistResponseDTO;
import cloud.loify.packages.playlist.exceptions.PlaylistNotFoundException;
import cloud.loify.packages.track.dto.GetTracksFromPlaylistResponseDTO;
import cloud.loify.packages.track.dto.SearchTrackResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller for managing playlists.
 */
@RestController
@RequestMapping("/v1/playlists")
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    /**
     * Retrieves a playlist by its ID.
     *
     * @param playlistId the ID of the playlist to retrieve.
     * @return a Mono containing the PlaylistResponseDTO.
     * @throws ResponseStatusException if the playlist is not found.
     */
    @GetMapping("/{playlistId}")
    public Mono<ResponseEntity<GetPlaylistResponseDTO>> getPlaylistById(@PathVariable String playlistId) {
        logger.info("Request to retrieve playlist with ID: {}", playlistId);
        return playlistService.getPlaylistById(playlistId)
                .map(playlist -> {
                    logger.info("Successfully retrieved playlist: {}", playlistId);
                    return ResponseEntity.ok(playlist);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null))) // Return 404 if not found
                .onErrorResume(error -> {
                    logger.error("Error retrieving playlist with ID {}: {}", playlistId, error.getMessage());
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving playlist", error));
                });
    }

    // TODO: This MAY be able to be removed - does it return BASICALLY the same content as `getPlaylistById()`

    /**
     * Retrieves all tracks in a specific playlist.
     *
     * @param playlistId the ID of the playlist.
     * @return a Mono containing the TracksDTO with all tracks.
     * @throws ResponseStatusException if the playlist is not found.
     */
    @GetMapping("/{playlistId}/tracks")
    public Mono<ResponseEntity<GetTracksFromPlaylistResponseDTO>> getAllTracksInPlaylist(@PathVariable String playlistId) {
        logger.info("Request to retrieve all tracks in playlist with ID: {}", playlistId);
        return playlistService.getAllTracksInPlaylist(playlistId)
                .map(tracks -> {
                    logger.info("Successfully retrieved tracks for playlist: {}", playlistId);
                    return ResponseEntity.ok(tracks);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null))) // Return 404 if not found
                .onErrorResume(error -> {
                    logger.error("Error retrieving tracks for playlist {}: {}", playlistId, error.getMessage());
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving tracks", error));
                });
    }

    // TODO: Consider changing to PUT Request - Ask ChatGPT

    /**
     * Adds tracks to a specific playlist.
     *
     * @param playlistId  the ID of the playlist.
     * @param requestBody the request body containing track details to add.
     * @return a Mono containing the ID of the updated playlist.
     * @throws ResponseStatusException if the playlist is not found or an error occurs.
     */
    @PostMapping("/{playlistId}/tracks")
    public Mono<ResponseEntity<String>> addTracksToPlaylist(@PathVariable String playlistId,
                                                            @RequestBody AddTracksToPlaylistRequestDTO requestBody) {
        logger.info("Request to add tracks to playlist with ID: {}", playlistId);
        return playlistService.addTracksToPlaylist(playlistId, requestBody)
                .map(updatedPlaylistId -> {
                    logger.info("Successfully added tracks to playlist: {}", playlistId);
                    return ResponseEntity.ok(updatedPlaylistId);
                })
                .onErrorResume(PlaylistNotFoundException.class, error -> {
                    logger.warn("Attempted to add tracks to a non-existing playlist: {}", playlistId);
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Playlist not found: " + playlistId));
                })
                .onErrorResume(error -> {
                    logger.error("Error adding tracks to playlist {}: {}", playlistId, error.getMessage());
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding tracks", error));
                });
    }

    /**
     * Retrieves and "loifys" all tracks in a specific playlist.
     *
     * @param playlistId the ID of the playlist.
     * @return a Flux containing TrackSearchResponseDTOs for all loifyed tracks.
     * @throws ResponseStatusException if the playlist is not found.
     */
    @GetMapping("/{playlistId}/loify")
    public Mono<ResponseEntity<Flux<SearchTrackResponseDTO>>> getAndLoifyAllTracksInPlaylist(@PathVariable String playlistId, @RequestParam String genre) {
        logger.info("Request to loify all tracks in playlist with ID: {}", playlistId);
        return playlistService.getAndLoifyAllTracksInPlaylist(playlistId, genre)
                .collectList()
                .map(loifyedTracks -> {
                    logger.info("Successfully loifyed tracks for playlist: {}", playlistId);
                    return ResponseEntity.ok(Flux.fromIterable(loifyedTracks));
                })
                .onErrorResume(error -> {
                    logger.error("Error loifying tracks for playlist {}: {}", playlistId, error.getMessage());
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error loifying tracks", error));
                });
    }

    // NOTE: This method is a combination of: [`createPlaylist()`, `addTracksToPlaylist()`]

    /**
     * Creates a loifyed playlist and adds the loifyed tracks.
     *
     * @param playlistId the ID of the playlist.
     * @return a CreatePlaylistResponseDTO with the created playlist details.
     * @throws ResponseStatusException if an error occurs during the operation.
     */
    @PostMapping("/{playlistId}/loify")
    public Mono<CreatePlaylistResponseDTO> createLoifyedPlaylistAndAddLoifyedTracks(@PathVariable String playlistId, @RequestParam String genre) {
        return playlistService.createLoifyedPlaylistAndAddLoifyedTracks(playlistId, genre);
    }
}
