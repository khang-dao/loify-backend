package cloud.loify.controller.spotify;

import cloud.loify.dto.AddTracksRequestDTO;
import cloud.loify.dto.CreatePlaylistRequestDTO;
import cloud.loify.dto.TrackNamesDTO;
import cloud.loify.service.SpotifyService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SpotifyController {

    private final SpotifyService spotifyService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public SpotifyController(SpotifyService spotifyService, OAuth2AuthorizedClientService authorizedClientService){
        this.spotifyService = spotifyService;
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/")
    public String home() {
        return "Hello world! :)";
    }

    @GetMapping("/spotify/login")
    public ResponseEntity homeSecured() {
        final String redirectUrl = "https://accounts.spotify.com/authorize?response_type=code&client_id=a8868f0f7a4f4a8885835375ff9ca242&scope=user-read-private%20user-read-email%20playlist-modify-public%20playlist-modify-private&state=gA9N5_ldRbWUO3YXklHrhnJ3XvYuO61nt7bsjPkw2cc%3D&redirect_uri=http://localhost:8080/api/spotify/login";
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("redirectUrl", redirectUrl);

        return ResponseEntity
                .status(HttpStatus.OK) // Status 200 OK
                .body(responseBody); // Response body as JSON
    }

    @Deprecated
    @GetMapping("/api/spotify/login")
    public void handleCallback(@AuthenticationPrincipal OAuth2User principal) {
        spotifyService.updateRequestHeadersWithAuthToken(principal);
    }

    @GetMapping("/api/spotify/users/{username}/playlists")
    public void getAllPlaylistsByUserId(@PathVariable String username) {
        System.out.println(spotifyService.getAllPlaylistsByUserId(username));
    }

    @GetMapping("/api/spotify/playlists/{playlistId}")
    public void getAllTrackNamesInPlaylist(@PathVariable String playlistId) {
        System.out.println(spotifyService.getAllTrackNamesInPlaylist(playlistId));
    }

//    @GetMapping("/api/spotify/playlists/{playlistId}")
//    public void getAllTrackIdsInPlaylist(@PathVariable String playlistId) {
//        System.out.println(spotifyService.getAllTrackIdsInPlaylist(playlistId));
//    }

    @GetMapping("/api/spotify/tracks/{trackName}")
    public void getFirstTrackIdByTrackName(@PathVariable String trackName) {
        System.out.println(spotifyService.getFirstTrackIdByTrackName(trackName));
    }

     @PostMapping("/api/spotify/users/{username}/playlists")
    public void createPlaylist(@PathVariable String username, @RequestBody CreatePlaylistRequestDTO requestBody) {
        spotifyService.createPlaylist(username, requestBody);
    }

    @PostMapping("/api/spotify/playlists/{playlistId}/tracks")
    public void addTracksToPlaylist(@PathVariable String playlistId, @RequestBody AddTracksRequestDTO requestBody) {
        spotifyService.addTracksToPlaylist(playlistId, requestBody);
    }

    @PostMapping("/api/spotify/tracks/loify")
    public List<String> getLoifyedTracks(@RequestBody TrackNamesDTO requestBody) {
        return spotifyService.getLoifyedTracks(requestBody);
    }
}
