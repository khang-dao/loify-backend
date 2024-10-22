package cloud.loify.packages.playlist;

import cloud.loify.dto.AddTracksRequestDTO;
import cloud.loify.dto.CreatePlaylistRequestDTO;
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

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping("/api/spotify/playlists/{playlistId}")
    public Mono<PlaylistResponseDTO> getPlaylistById(@PathVariable String playlistId) {
        return playlistService.getPlaylistById(playlistId);
    }

//    @GetMapping("/api/spotify/playlists/{playlistId}/tracks")
//    public Mono<TracksDTO> getAllTracksInPlaylist(@PathVariable String playlistId) {
//        return playlistService.getAllTracksInPlaylist(playlistId);
//    }
//
//    @PostMapping("/api/spotify/users/{username}/playlists")
//    public Mono<CreatePlaylistResponseDTO> createPlaylist(@PathVariable String username, @RequestBody CreatePlaylistRequestDTO requestBody) {
//        return playlistService.createPlaylist(username, requestBody);
//    }
//
//    @PostMapping("/api/spotify/playlists/{playlistId}/tracks")
//    public Mono<String> addTracksToPlaylist(@PathVariable String playlistId, @RequestBody AddTracksRequestDTO requestBody) {
//        return playlistService.addTracksToPlaylist(playlistId, requestBody);
//    }
//
//    @GetMapping("/api/spotify/playlists/{playlistId}/tracks/loify")
//    public Flux<TrackSearchResponseDTO> getAndLoifyAllTracksInPlaylist(@PathVariable String playlistId) {
//        return playlistService.getAndLoifyAllTracksInPlaylist(playlistId);
//    }
//
//    // ^This method is a combo of: [`createPlaylist()`, `addTracksToPlaylist()`]
//    @PostMapping("/api/spotify/playlists/{playlistId}/tracks/loify")
//    public CreatePlaylistResponseDTO createLoifyedPlaylistAndAddLoifyedTracks(@PathVariable String playlistId) {
//        return playlistService.createLoifyedPlaylistAndAddLoifyedTracks(playlistId);
//    }
}
