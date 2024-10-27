package cloud.loify.packages.me;

import cloud.loify.dto.CreatePlaylistRequestDTO;
import cloud.loify.dto.CreatePlaylistResponseDTO;
import cloud.loify.dto.PlaylistDTO;
import cloud.loify.packages.auth.AuthService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MeService {

    private final AuthService auth;

    public MeService(AuthService auth) {
        this.auth = auth;
    }

    // Swap `PlaylistDTO` with `UserPlaylistResponseDTO`
    public Mono<PlaylistDTO> getAllPlaylistsByCurrentUser() {
        return this.auth.webClient.get()
                .uri("/v1/me/playlists")
                .retrieve()
                .bodyToMono(PlaylistDTO.class);
    }

    public Mono<CreatePlaylistResponseDTO> createPlaylistForCurrentUser(CreatePlaylistRequestDTO requestBody) {
        return this.auth.getUserProfile()
                .map(user -> user.id())
                .flatMap(userId -> this.auth.webClient.post()
                        .uri("v1/users/" + userId + "/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(CreatePlaylistResponseDTO.class)
                );
    }

}
