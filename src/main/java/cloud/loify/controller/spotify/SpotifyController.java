package cloud.loify.controller.spotify;

import cloud.loify.dto.*;
import cloud.loify.dto.response.PlaylistResponseDTO;
import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.dto.track.TrackNamesDTO;
import cloud.loify.dto.track.TracksDTO;
import cloud.loify.service.SpotifyService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService){
        this.spotifyService = spotifyService;
    }

    @GetMapping("/")
    public String home() {
        return "Hello world! :)";
    }

    @GetMapping("/auth-check")
    public ResponseEntity<String> isLoggedIn() {
        return spotifyService.validateToken()
                ? ResponseEntity.ok("User is logged in (authenticated)")
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or session expired (not authenticated)");
    }

    // Isn't this weird cause this is the login route AND callback? (possibly we dont need a login route... becquse all routes require login)
    @GetMapping("/api/spotify/login")
    public ResponseEntity<Void> loginCallback(@AuthenticationPrincipal OAuth2User principal, HttpServletResponse response) {
        spotifyService.updateRequestHeadersWithAuthToken(principal);
        spotifyService.setUserProfile();

        // Redirect to the frontend application
        response.setStatus(HttpServletResponse.SC_FOUND); // HTTP 302
        response.setHeader("Location", "http://localhost:3000/loify"); // Replace with your actual frontend URL
        return new ResponseEntity<>(HttpStatus.FOUND);
    }

    @GetMapping("/api/spotify/playlists/{playlistId}")
    public Mono<PlaylistResponseDTO> getPlaylistById(@PathVariable String playlistId) {
        return spotifyService.getPlaylistById(playlistId);
    }

    @GetMapping("/api/spotify/me/playlists")
    public Mono<PlaylistDTO> getAllPlaylistsByCurrentUser() {
        return spotifyService.getAllPlaylistsByCurrentUser();
    }

    @Deprecated
    @GetMapping("/api/spotify/users/{username}/playlists")
    public void getAllPlaylistsByUserId(@PathVariable String username) {
        System.out.println(spotifyService.getAllPlaylistsByUserId(username));
    }

    @GetMapping("/api/spotify/playlists/{playlistId}/tracks")
    public Mono<TracksDTO> getAllTracksInPlaylist(@PathVariable String playlistId) {
        return spotifyService.getAllTracksInPlaylist(playlistId);
    }

    @Deprecated
//    @GetMapping("/api/spotify/playlists/{playlistId}")
//    public void getAllTrackNamesInPlaylist(@PathVariable String playlistId) {
//        System.out.println(spotifyService.getAllTrackNamesInPlaylist(playlistId));
//    }

//    @GetMapping("/api/spotify/playlists/{playlistId}")
//    public void getAllTrackIdsInPlaylist(@PathVariable String playlistId) {
//        System.out.println(spotifyService.getAllTrackIdsInPlaylist(playlistId));
//    }

    @GetMapping("/api/spotify/tracks/{trackName}")
    public Mono<TrackSearchResponseDTO> getFirstTrackByTrackName(@PathVariable String trackName) {
        return spotifyService.getFirstTrackByTrackName(trackName);
    }

    @Deprecated
//    @GetMapping("/api/spotify/tracks/{trackName}")
//    public void getFirstTrackIdByTrackName(@PathVariable String trackName) {
//        System.out.println(spotifyService.getFirstTrackIdByTrackName(trackName));
//    }

    @PostMapping("/api/spotify/users/{username}/playlists")
    public Mono<CreatePlaylistResponseDTO> createPlaylist(@PathVariable String username, @RequestBody CreatePlaylistRequestDTO requestBody) {
        return spotifyService.createPlaylist(username, requestBody);
    }

    @PostMapping("/api/spotify/playlists/{playlistId}/tracks")
    public Mono<String> addTracksToPlaylist(@PathVariable String playlistId, @RequestBody AddTracksRequestDTO requestBody) {
        return spotifyService.addTracksToPlaylist(playlistId, requestBody);
    }

    @Deprecated
    @PostMapping("/api/spotify/tracks/loify")
    public List<String> getLoifyedTracks(@RequestBody TrackNamesDTO requestBody) {
        return spotifyService.getLoifyedTracks(requestBody);
    }

    @GetMapping("/api/spotify/playlists/{playlistId}/tracks/loify")
    public Flux<TrackSearchResponseDTO> getAndLoifyAllTracksInPlaylist(@PathVariable String playlistId) {
        return spotifyService.getAndLoifyAllTracksInPlaylist(playlistId);
    }

    // ^This method is a combo of: [`createPlaylist()`, `addTracksToPlaylist()`]
    @PostMapping("/api/spotify/playlists/{playlistId}/tracks/loify")
    public CreatePlaylistResponseDTO createLoifyedPlaylistAndAddLoifyedTracks(@PathVariable String playlistId) {
        return spotifyService.createLoifyedPlaylistAndAddLoifyedTracks(playlistId);
    }
}
