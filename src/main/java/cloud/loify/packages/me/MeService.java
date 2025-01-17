package cloud.loify.packages.me;

import cloud.loify.packages.auth.AuthService;
import cloud.loify.packages.me.dto.GetUserPlaylistsResponseDTO;
import cloud.loify.packages.me.dto.GetUserResponseDTO;
import cloud.loify.packages.playlist.dto.CreatePlaylistRequestDTO;
import cloud.loify.packages.playlist.dto.CreatePlaylistResponseDTO;
import cloud.loify.packages.playlist.dto.PlaylistDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class MeService {

    private static final Logger logger = LoggerFactory.getLogger(MeService.class);
    private final WebClient webClient;
    private final AuthService auth;

    public MeService(AuthService auth, WebClient webClient) {
        this.auth = auth;
        this.webClient = webClient;
    }

    public Mono<GetUserPlaylistsResponseDTO> getAllPlaylistsByCurrentUser() {
        logger.info("Retrieving all playlists for the current user.");
        return this.webClient.get()
                .uri("/me/playlists")
                .retrieve()
                .bodyToMono(GetUserPlaylistsResponseDTO.class)
                .doOnSuccess(playlists -> {
                    logger.info("Successfully retrieved playlists for the current user.");
                    logger.info(String.valueOf(playlists));
                })
                .doOnError(err -> logger.error("Error retrieving playlists: {}", err.getMessage()));
    }

    public Mono<CreatePlaylistResponseDTO> createPlaylistForCurrentUser(CreatePlaylistRequestDTO requestBody) {
        logger.info("Creating a new playlist for the current user with request body: {}", requestBody);
        return this.auth.getUserProfile()
                .map(GetUserResponseDTO::id)
                .flatMap(userId -> this.webClient.post()
                        .uri("/users/" + userId + "/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(CreatePlaylistResponseDTO.class)
                        .doOnSuccess(response -> logger.info("Successfully created playlist for user ID: {}", userId))
                        .doOnError(err -> logger.error("Error creating playlist for user ID: {} - {}", userId, err.getMessage()))
                );
    }

    public Mono<Void> deletePlaylistById(String playlistId) {
        logger.info("Deleting playlist with ID: {}", playlistId);
        return this.webClient.delete()
                .uri("/playlists/{playlistId}/followers", playlistId) // Spotify’s delete endpoint for playlists
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> logger.info("Deleted playlist with ID: {}", playlistId))
                .doOnError(err -> logger.error("Error deleting playlist with ID {}: {}", playlistId, err.getMessage()));
    }


    public Mono<Void> deleteAllLoifyPlaylists() {
        logger.info("Deleting all playlists with 'loify' in the name.");
        return getAllPlaylistsByCurrentUser()
                .flatMapMany(response -> Flux.fromIterable(response.items()))
                .filter(playlist -> playlist != null && playlist.name() != null && playlist.name().toLowerCase().contains("loify"))
                .map(PlaylistDetailsDTO::id)
                .delayElements(Duration.ofMillis(200))
                .flatMap(this::deletePlaylistById)
                .then()
                .doOnSuccess(v -> logger.info("Successfully deleted all playlists with 'loify' in the name."))
                .doOnError(err -> logger.error("Error deleting playlists: {}", err.getMessage()));

    }

}
