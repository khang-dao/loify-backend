package cloud.loify.packages.me;

import cloud.loify.dto.CreatePlaylistRequestDTO;
import cloud.loify.dto.CreatePlaylistResponseDTO;
import cloud.loify.dto.PlaylistDTO;
import cloud.loify.dto.response.UserDetailsResponseDTO;
import cloud.loify.packages.playlist.PlaylistService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final MeService meService;

    public MeController(MeService meService) {
        this.meService = meService;
    }

    @GetMapping("/playlists")
    public Mono<PlaylistDTO> getAllPlaylistsByCurrentUser() {
        return meService.getAllPlaylistsByCurrentUser();
    }

    // TODO: Possibly refactor to /me method
    @PostMapping("/playlists")
    public Mono<CreatePlaylistResponseDTO> createPlaylist(@RequestBody CreatePlaylistRequestDTO requestBody) {
        return meService.createPlaylist(requestBody);
    }
}
