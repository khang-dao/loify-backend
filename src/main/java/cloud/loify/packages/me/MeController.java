package cloud.loify.packages.me;

import cloud.loify.packages.playlist.dto.CreatePlaylistRequestDTO;
import cloud.loify.packages.playlist.dto.CreatePlaylistResponseDTO;
import cloud.loify.packages.me.dto.GetUserPlaylistsResponseDTO;
import cloud.loify.packages.me.exceptions.InvalidRequestException;
import cloud.loify.packages.me.exceptions.PlaylistCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Controller for managing user playlists.
 */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final MeService meService;
    private static final Logger logger = LoggerFactory.getLogger(MeController.class);

    public MeController(MeService meService) {
        this.meService = meService;
    }

    /**
     * Retrieves all playlists for the current user.
     *
     * @return a Mono containing the user's playlists.
     * @throws ResponseStatusException if an error occurs while retrieving playlists.
     */
    @GetMapping("/playlists")
    public Mono<GetUserPlaylistsResponseDTO> getPlaylists() {
        logger.info("Request to retrieve all playlists for the current user.");
        return meService.getAllPlaylistsByCurrentUser()
                .doOnSuccess(playlists -> logger.info("Successfully retrieved playlists for the current user."))
                .onErrorResume(error -> {
                    logger.error("Error retrieving playlists: {}", error.getMessage());
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Unable to retrieve playlists", error
                    ));
                });
    }

    /**
     * Creates a new playlist for the current user.
     *
     * @param requestBody The details of the playlist to be created.
     * @return a Mono containing the response with the created playlist details.
     * @throws InvalidRequestException if the request body is invalid.
     * @throws PlaylistCreationException if an error occurs during playlist creation.
     */
    @PostMapping("/playlists")
    public Mono<CreatePlaylistResponseDTO> createPlaylist(@RequestBody CreatePlaylistRequestDTO requestBody) {
        logger.info("Request to create a new playlist: {}", requestBody);

        return validateCreatePlaylistRequest(requestBody)
                .flatMap(meService::createPlaylistForCurrentUser)
                .doOnSuccess(response -> logger.info("Successfully created playlist: {}", response))
                .onErrorResume(InvalidRequestException.class, error -> {
                    logger.warn("Invalid request for playlist creation: {}", error.getMessage());
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Invalid playlist creation request", error));
                })
                .onErrorResume(PlaylistCreationException.class, error -> {
                    logger.error("Error during playlist creation: {}", error.getMessage());
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Error creating playlist", error));
                })
                .onErrorResume(error -> {
                    logger.error("Unexpected error: {}", error.getMessage());
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", error));
                });
    }

    /**
     * Validates the create playlist request.
     *
     * @param request The playlist creation request body.
     * @return Mono<CreatePlaylistRequestDTO> if valid; error if invalid.
     */
    private Mono<CreatePlaylistRequestDTO> validateCreatePlaylistRequest(CreatePlaylistRequestDTO request) {
        if (request == null) {
            logger.warn("Received null request for playlist creation.");
            return Mono.error(new InvalidRequestException("Playlist is required"));
        }
        return Mono.just(request);
    }


    /**
     * Deletes all playlists with "loify" in the name for the current user.
     *
     * @return Mono<Void> upon completion, with status indicating success or error.
     */
    @DeleteMapping("/playlists/loify")
    public Mono<Void> deleteAllLoifyedPlaylists() {
        logger.info("Request to delete all playlists with 'loify' in the name.");

        return meService.deleteAllLoifyPlaylists()
                .then(Mono.just(ResponseEntity.ok("Successfully deleted all playlists with 'loify' in the name.")))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("No playlists with 'loify' in the name found.")))
                .onErrorResume(e -> {
                    logger.error("An error occurred during playlist deletion: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete playlists due to an internal error."));
                }).then();
    }
}
