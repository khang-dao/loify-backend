package cloud.loify.packages.playlist;

import cloud.loify.dto.AddTracksRequestDTO;
import cloud.loify.dto.CreatePlaylistResponseDTO;
import cloud.loify.dto.response.PlaylistResponseDTO;
import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.dto.track.TracksDTO;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    // TODO: Think about which controller/service methods I actually need,
    // TODO: Because I think some of them are legacy from dev/testing
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping("/{playlistId}")
    public Mono<PlaylistResponseDTO> getPlaylistById(@PathVariable String playlistId) {
        return playlistService.getPlaylistById(playlistId);
    }

    // TODO: This MAY be able to be removed - does it return BASICALLY the same content as `getPlaylistById()`
    @GetMapping("/{playlistId}/tracks")
    public Mono<TracksDTO> getAllTracksInPlaylist(@PathVariable String playlistId) {
        return playlistService.getAllTracksInPlaylist(playlistId);
    }

    // TODO: Consider changing to PUT Request - Ask ChatGPT
    @PostMapping("/{playlistId}/tracks")
    public Mono<String> addTracksToPlaylist(@PathVariable String playlistId, @RequestBody AddTracksRequestDTO requestBody) {
        return playlistService.addTracksToPlaylist(playlistId, requestBody);
    }

    @GetMapping("/{playlistId}/loify")
    public Flux<TrackSearchResponseDTO> getAndLoifyAllTracksInPlaylist(@PathVariable String playlistId) {
        return playlistService.getAndLoifyAllTracksInPlaylist(playlistId);
    }

    // NOTE: This method is a combo of: [`createPlaylist()`, `addTracksToPlaylist()`]
    @PostMapping("/{playlistId}/loify")
    public CreatePlaylistResponseDTO createLoifyedPlaylistAndAddLoifyedTracks(@PathVariable String playlistId) {
        return playlistService.createLoifyedPlaylistAndAddLoifyedTracks(playlistId);
    }
}
