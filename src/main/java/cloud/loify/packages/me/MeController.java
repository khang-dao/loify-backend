package cloud.loify.packages.me;

import cloud.loify.dto.CreatePlaylistRequestDTO;
import cloud.loify.dto.CreatePlaylistResponseDTO;
import cloud.loify.dto.PlaylistDTO;
import cloud.loify.packages.me.exceptions.InvalidRequestException;
import cloud.loify.packages.me.exceptions.PlaylistCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final MeService meService;

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
    public Mono<PlaylistDTO> getPlaylists() {
        return meService.getAllPlaylistsByCurrentUser()
                .onErrorResume(error -> {
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
        return validateCreatePlaylistRequest(requestBody)
                .flatMap(meService::createPlaylistForCurrentUser)
                .onErrorResume(InvalidRequestException.class, error -> Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid playlist creation request", error)))
                .onErrorResume(PlaylistCreationException.class, error -> Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Error creating playlist", error)))
                .onErrorResume(error -> Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", error)));
    }

    /**
     * Validates the create playlist request.
     *
     * @param request The playlist creation request body.
     * @return Mono<CreatePlaylistRequestDTO> if valid; error if invalid.
     */
    private Mono<CreatePlaylistRequestDTO> validateCreatePlaylistRequest(CreatePlaylistRequestDTO request) {
        if (request == null) {
            return Mono.error(new InvalidRequestException("Playlist is required"));
        }
        return Mono.just(request);
    }
}
