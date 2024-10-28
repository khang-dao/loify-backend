package cloud.loify.packages.me;

import cloud.loify.packages.playlist.dto.CreatePlaylistRequestDTO;
import cloud.loify.packages.playlist.dto.CreatePlaylistResponseDTO;
import cloud.loify.packages.me.dto.GetUserPlaylistsResponseDTO;
import cloud.loify.packages.auth.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MeService {

    private final AuthService auth;

    // Logger instance
    private static final Logger logger = LoggerFactory.getLogger(MeService.class);

    public MeService(AuthService auth) {
        this.auth = auth;
    }

    // Swap `PlaylistDTO` with `UserPlaylistResponseDTO`
    public Mono<GetUserPlaylistsResponseDTO> getAllPlaylistsByCurrentUser() {
        logger.info("Retrieving all playlists for the current user.");
        return this.auth.webClient.get()
                .uri("/v1/me/playlists")
                .retrieve()
                .bodyToMono(GetUserPlaylistsResponseDTO.class)
                .doOnSuccess(playlists -> logger.info("Successfully retrieved playlists for the current user."))
                .doOnError(err -> logger.error("Error retrieving playlists: {}", err.getMessage()));
    }

    public Mono<CreatePlaylistResponseDTO> createPlaylistForCurrentUser(CreatePlaylistRequestDTO requestBody) {
        logger.info("Creating a new playlist for the current user with request body: {}", requestBody);
        return this.auth.getUserProfile()
                .map(user -> user.id())
                .flatMap(userId -> this.auth.webClient.post()
                        .uri("v1/users/" + userId + "/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(CreatePlaylistResponseDTO.class)
                        .doOnSuccess(response -> logger.info("Successfully created playlist for user ID: {}", userId))
                        .doOnError(err -> logger.error("Error creating playlist for user ID: {} - {}", userId, err.getMessage()))
                );
    }

}