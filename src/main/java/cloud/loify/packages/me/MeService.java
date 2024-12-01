package cloud.loify.packages.me;

import cloud.loify.packages.playlist.dto.CreatePlaylistRequestDTO;
import cloud.loify.packages.playlist.dto.CreatePlaylistResponseDTO;
import cloud.loify.packages.me.dto.GetUserPlaylistsResponseDTO;
import cloud.loify.packages.auth.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class MeService {

    private final WebClient webClient;
    private final AuthService auth;

    // Logger instance
    private static final Logger logger = LoggerFactory.getLogger(MeService.class);

    public MeService(AuthService auth, WebClient.Builder webClientBuilder, WebClient webClient) {
        this.auth = auth;
        this.webClient = webClient;
    }

    public Mono<GetUserPlaylistsResponseDTO> getAllPlaylistsByCurrentUser() {
        logger.info("Retrieving all playlists for the current user.");
        return webClient.get()
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
                .flatMap(userId -> this.webClient.post()
                        .uri("v1/users/" + userId + "/playlists")
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
                .uri("/v1/playlists/{playlistId}/followers", playlistId) // Spotify’s delete endpoint for playlists
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> logger.info("Deleted playlist with ID: {}", playlistId))
                .doOnError(err -> logger.error("Error deleting playlist with ID {}: {}", playlistId, err.getMessage()));
    }


    public Mono<Void> deleteAllLoifyPlaylists() {
        logger.info("Deleting all playlists with 'loify' in the name.");
        return getAllPlaylistsByCurrentUser()
                .flatMapMany(response -> Flux.fromIterable(response.items()))
                .filter(playlist -> playlist.name().toLowerCase().contains("loify"))
                .map(playlist -> playlist.id())
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .delayElements(Duration.ofMillis(200))  // Necessary to avoid 502 Bad Gateway error
                .flatMap(this::deletePlaylistById)
                .then()
                .doOnSuccess(v -> logger.info("Successfully deleted all playlists with 'loify' in the name."))
                .doOnError(err -> logger.error("Error deleting playlists: {}", err.getMessage()));
    }

}
