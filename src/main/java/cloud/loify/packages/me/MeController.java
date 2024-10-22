package cloud.loify.packages.me;

import cloud.loify.dto.PlaylistDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

//    @GetMapping("/playlists")
//    public Mono<PlaylistDTO> getAllPlaylistsByCurrentUser() {
//        return spotifyService.getAllPlaylistsByCurrentUser();
//    }
}
