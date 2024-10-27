package cloud.loify.packages.playlist;

import cloud.loify.dto.AddTracksRequestDTO;
import cloud.loify.dto.CreatePlaylistResponseDTO;
import cloud.loify.dto.response.PlaylistResponseDTO;
import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.dto.track.TracksDTO;
import cloud.loify.packages.playlist.exceptions.PlaylistNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/playlists")
public class PlaylistController {

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
    public Mono<ResponseEntity<PlaylistResponseDTO>> getPlaylistById(@PathVariable String playlistId) {
        return playlistService.getPlaylistById(playlistId)
                .map(playlist -> ResponseEntity.ok(playlist))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null))) // Return 404 if not found
                .onErrorResume(error -> Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving playlist", error)));
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
    public Mono<ResponseEntity<TracksDTO>> getAllTracksInPlaylist(@PathVariable String playlistId) {
        return playlistService.getAllTracksInPlaylist(playlistId)
                .map(tracks -> ResponseEntity.ok(tracks))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null))) // Return 404 if not found
                .onErrorResume(error -> Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving tracks", error)));
    }

    // TODO: Consider changing to PUT Request - Ask ChatGPT
    /**
     * Adds tracks to a specific playlist.
     *
     * @param playlistId the ID of the playlist.
     * @param requestBody the request body containing track details to add.
     * @return a Mono containing the ID of the updated playlist.
     * @throws ResponseStatusException if the playlist is not found or an error occurs.
     */
    @PostMapping("/{playlistId}/tracks")
    public Mono<ResponseEntity<String>> addTracksToPlaylist(@PathVariable String playlistId,
                                                            @RequestBody AddTracksRequestDTO requestBody) {
        return playlistService.addTracksToPlaylist(playlistId, requestBody)
                .map(updatedPlaylistId -> ResponseEntity.ok(updatedPlaylistId))
                .onErrorResume(PlaylistNotFoundException.class, error -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Playlist not found: " + playlistId)))
                .onErrorResume(error -> Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Error adding tracks", error)));
    }

    /**
     * Retrieves and "loifies" all tracks in a specific playlist.
     *
     * @param playlistId the ID of the playlist.
     * @return a Flux containing TrackSearchResponseDTOs for all loified tracks.
     * @throws ResponseStatusException if the playlist is not found.
     */
    @GetMapping("/{playlistId}/loify")
    public Mono<ResponseEntity<Flux<TrackSearchResponseDTO>>> getAndLoifyAllTracksInPlaylist(@PathVariable String playlistId) {
        return playlistService.getAndLoifyAllTracksInPlaylist(playlistId)
                .collectList()
                .map(loifiedTracks -> ResponseEntity.ok(Flux.fromIterable(loifiedTracks)))
                .onErrorResume(error -> Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Error loifying tracks", error)));
    }

    // NOTE: This method is a combo of: [`createPlaylist()`, `addTracksToPlaylist()`]
    /**
     * Creates a loified playlist and adds the loified tracks.
     *
     * @param playlistId the ID of the playlist.
     * @return a CreatePlaylistResponseDTO with the created playlist details.
     * @throws ResponseStatusException if an error occurs during the operation.
     */
    @PostMapping("/{playlistId}/loify")
    public CreatePlaylistResponseDTO createLoifyedPlaylistAndAddLoifyedTracks(@PathVariable String playlistId) {
        return playlistService.createLoifyedPlaylistAndAddLoifyedTracks(playlistId);
    }
}
