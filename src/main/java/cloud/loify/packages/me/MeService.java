package cloud.loify.packages.me;

import cloud.loify.dto.PlaylistDTO;
import cloud.loify.dto.response.UserDetailsResponseDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MeService {

//    public Mono<UserDetailsResponseDTO> getUserProfile() {
//        return this.webClient.get()
//                .uri("/v1/me")
//                .retrieve()
//                .bodyToMono(UserDetailsResponseDTO.class);
//    }
//
//    public void setUserProfile() {
//        this.userProfile = this.getUserProfile().block();
//        System.out.println("USER PROFILE::::  " + userProfile);
//    }
//
//
//    // Swap `PlaylistDTO` with `UserPlaylistResponseDTO`
//    public Mono<PlaylistDTO> getAllPlaylistsByCurrentUser() {
//        return this.webClient.get()
//                .uri("/v1/me/playlists")
//                .retrieve()
//                .bodyToMono(PlaylistDTO.class);
//    }
}
