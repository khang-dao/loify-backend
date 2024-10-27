package cloud.loify.service;

import cloud.loify.dto.*;
import cloud.loify.dto.response.PlaylistResponseDTO;
import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.dto.response.UserDetailsResponseDTO;
import cloud.loify.dto.track.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Base64;

@Service
public class SpotifyService {

    private final WebClient.Builder webClientBuilder;
    private static final String BASE_URL = "https://api.spotify.com";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    private WebClient webClient;
    private UserDetailsResponseDTO userProfile;

    @Deprecated
    private String authToken;
    @Deprecated
    public String authCode;

    private final OAuth2AuthorizedClientService authorizedClientService;

    public SpotifyService(WebClient.Builder webClientBuilder, OAuth2AuthorizedClientService authorizedClientService) {
        this.webClientBuilder = webClientBuilder;
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();

        this.authorizedClientService = authorizedClientService;
    }

    public Boolean validateToken() {
        try {
            this.getUserProfile().block();
            return true;  // Token is valid if no exceptions are thrown
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                return false;   // If the token is invalid or expired, return false
            }
            throw e;  // Rethrow in case of other errors
        }
    }

    public void updateRequestHeadersWithAuthToken(@AuthenticationPrincipal OAuth2User principal) {
        OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient("spotify", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = client.getRefreshToken().getTokenValue();

        this.webClient = this.webClientBuilder
                .filter(ExchangeFilterFunction.ofRequestProcessor(request -> {
                    ClientRequest updatedRequest = ClientRequest.from(request)
                            .header("Authorization", "Bearer " + accessToken)
                            .build();
                    return Mono.just(updatedRequest);
                }))
                .build();

        System.out.println("AUTHENTICATED :) happy coding");
        System.out.println("accessToken: " + accessToken);
        System.out.println("refreshToken: " + refreshToken);
    }

    public Mono<UserDetailsResponseDTO> getUserProfile() {
        return this.webClient.get()
                .uri("/v1/me")
                .retrieve()
                .bodyToMono(UserDetailsResponseDTO.class);
    }

    public void setUserProfile() {
        this.userProfile = this.getUserProfile().block();
        System.out.println("USER PROFILE::::  " + userProfile);
    }

    // Swap `PlaylistDTO` with `UserPlaylistResponseDTO`
    public Mono<PlaylistDTO> getAllPlaylistsByCurrentUser() {
        return this.webClient.get()
                .uri("/v1/me/playlists")
                .retrieve()
                .bodyToMono(PlaylistDTO.class);
    }

    @Deprecated
    public List<String> getAllPlaylistsByUserId(String userId) {
        return this.webClient.get()
                .uri("/v1/users/" + userId + "/playlists")
                .retrieve()
                .bodyToMono(String.class)
                .map(JSONObject::new)
                .map(jsonObject -> {
                    JSONArray items = jsonObject.getJSONArray("items");
                    List<String> playlistIds = new ArrayList<>();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        playlistIds.add(item.getString("id"));
                    }
                    return playlistIds;
                })
                .block();
    }

    public Mono<TracksDTO> getAllTracksInPlaylist(String playlistId) {
        return this.webClient.get()
                .uri("/v1/playlists/" + playlistId + "/tracks")
                .retrieve()
                .bodyToMono(TracksDTO.class);
    }

    @Deprecated
    public List<String> getAllTrackNamesInPlaylist(String playlistId) {
        JSONArray tracks =  this.webClient.get()
                .uri("/v1/playlists/" + playlistId + "/tracks")
                .retrieve()
                .bodyToMono(String.class)
                .map(JSONObject::new)
                .map(response -> response.getJSONObject("tracks").getJSONArray("items"))
                .block();

        Stream<JSONObject> tracksStream = IntStream.range(0, tracks.length()).mapToObj(tracks::getJSONObject);
        List<String> trackNames = tracksStream.map(t -> t.getJSONObject("track").getString("name")).collect(Collectors.toList());

        return trackNames;
    }

//    public List<String>  getAllTrackIdsInPlaylist(String playlistId) {
//        JSONArray tracks =  this.webClient.get()
//                .uri("/v1/playlists/" + playlistId)
//                .retrieve()
//                .bodyToMono(String.class)
//                .map(JSONObject::new)
//                .map(response -> response.getJSONObject("tracks").getJSONArray("items"))
//                .block();
//
//        Stream<JSONObject> tracksStream = IntStream.range(0, tracks.length()).mapToObj(tracks::getJSONObject);
//        List<String> trackNames = tracksStream.map(t -> t.getJSONObject("track").getString("id")).collect(Collectors.toList());
//        return trackNames;
//    }


    public Mono<TrackSearchResponseDTO> getFirstTrackByTrackName(String trackName) {
        try {
            return this.webClient.get()
                    .uri("/v1/search?q=track:" + trackName + "&type=track&limit=1")
                    .retrieve()
                    .bodyToMono(TrackSearchResponseDTO.class);
        }
        catch (JSONException err) {
            System.out.println("Song could not be Loify-ed: " + err);
        }
        return null;
    }

    @Deprecated
    public String getFirstTrackIdByTrackName(String trackName) {
        try {
            String trackId = this.webClient.get()
                    .uri("/v1/search?q=track:" + trackName + "&type=track&limit=1")
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(JSONObject::new)
                    .map(response -> {
                                System.out.println(response);
                                return response.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getString("id");
                            }
                    )
                    .block();
            return trackId;
        }
        catch (JSONException err) {
            System.out.println("Song could not be Loify-ed: " + err);
        }
        return null;
    }

    public Mono<PlaylistResponseDTO> getPlaylistById(String playlistId) {
        return this.webClient.get()
                .uri("v1/playlists/" + playlistId)
                .retrieve()
                .bodyToMono(PlaylistResponseDTO.class);
    }

    public Mono<CreatePlaylistResponseDTO> createPlaylist(String username, CreatePlaylistRequestDTO requestBody) {
        return this.webClient.post()
                .uri("v1/users/" + username + "/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(CreatePlaylistResponseDTO.class);
//                .block();

//        System.out.println("Playlist Created: " + responseBody);
    }

    public Mono<String> addTracksToPlaylist(String playlistId, AddTracksRequestDTO requestBody) {
        System.out.println(requestBody);
        System.out.println("Adding tracks to playlist...");
        return this.webClient.post()
                .uri("v1/playlists/" + playlistId + "/tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);  // NOTE: String = `snapshot_id`
    }

    private String loifyPlaylistName(String playlistName){
        return "loify - " + playlistName + " 🍃";
    }

    private String loifyPlaylistDescription(String playlistName){
        return "a loify-ed version of playlist: " + playlistName;
    }

    private String loifyTrackName(String trackName){
        return trackName + " lofi";
    }

    @Deprecated
    public List<String> getLoifyedTracks(TrackNamesDTO requestBody) {
        List<String> trackNames = requestBody.trackNames();
        Stream<String> loifyedTrackNames = trackNames.stream().map(this::loifyTrackName);
        return loifyedTrackNames
                .map(this::getFirstTrackIdByTrackName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public Flux<TrackSearchResponseDTO> getAndLoifyAllTracksInPlaylist(String playlistId) {
//    public void getAndLoifyAllTracksInPlaylist(String playlistId) {
        // STEP 1: Get all `tracks` in playlist --- (maybe just call `getAllTracksInPlaylist()`)
        TracksDTO tracks = this.getAllTracksInPlaylist(playlistId).block();

        // STEP 2: Extract all `tracksNames` in playlist
        Stream<String> trackNames = tracks.items()
                .stream()
                .map((t) -> (TrackItemDTO) t)
                .map((t) -> t.track().name());

        // STEP 3: "loify" all `trackNames` - `loifyedTrackNames = trackNames.map(this::loifyTrackName)`
        Stream<String> loifyedTrackNames = trackNames.map(this::loifyTrackName);

        // STEP 3: Get all "loify-ed" tracks (by using FIRST_TRACK algo) - `loifedTracks = loifyedTrackNames.map(this::getFirstTrackByTrackName)`
        Flux<TrackSearchResponseDTO> loifyedTracks = Flux.fromStream(loifyedTrackNames
                .map(this::getFirstTrackByTrackName))
                .flatMap(mono -> mono);  // Flatten the Mono<Track> into a Flux<Track>

        // STEP 4: Now we have all Mono<TrackDTO> `loifyedTracks`, simply return - `return loifyedTracks`
        return loifyedTracks;

    }

    // TODO: Make this atomic - because sometimes the playlist is created, but the songs aren't added
    // TODO: ^ need to make it so that if one fails, then it's as if the method was never called
    // TODO: ^ (might involve deleting the playlist if the transaction fails)
    public CreatePlaylistResponseDTO createLoifyedPlaylistAndAddLoifyedTracks(@PathVariable String playlistId) {
        // STEP 0: Get current playlist details
        // STEP 1: Create new (empty 🍃) playlist
        PlaylistResponseDTO currentPlaylist = this.getPlaylistById(playlistId).block();
        String currentPlaylistName = currentPlaylist.name();
        String loifyPlaylistName = this.loifyPlaylistName(currentPlaylistName);
        String loifyPlaylistDescription = this.loifyPlaylistDescription(currentPlaylistName);

        // TODO: Check if playlist exists already
        CreatePlaylistRequestDTO createPlaylistReqBody = new CreatePlaylistRequestDTO(loifyPlaylistName, loifyPlaylistDescription, true, true);

        CreatePlaylistResponseDTO response = this.createPlaylist(this.userProfile.id(), createPlaylistReqBody).block();
        String loifyPlaylistId = response.id();

        // STEP 2: Update (empty 🍃) playlist - image
        try {
            String currentPlaylistImage = currentPlaylist.images().get(0).url();
            String loifyPlaylistImage = this.loifyPlaylistImage(currentPlaylistImage);
            this.updatePlaylistImage(loifyPlaylistId, loifyPlaylistImage).block();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            // TODO: Implement default cover image
            System.out.println("Original Playlist does not have cover image... fallback to default loify image");
        }


        // STEP 3: Loify all tracks in playlist -> return an array of 🍃tracks -> Flux<TrackSearchResponseDTO>.block()
        List<TrackSearchResponseDTO> loifyedTracks = this.getAndLoifyAllTracksInPlaylist(playlistId).collectList().block();
        System.out.println(loifyedTracks);

        // STEP 4: Add loify-ed tracks -> to (empty 🍃) playlist
        List<String> uris = loifyedTracks
                .stream()
                .map((t) -> {
                    try {
                        return (TrackItemObjectDTO) t.tracks().items().get(0);
                    } catch (Exception e) {
                        System.out.println("No track found - skipping item...");
                        return null;
                    }
                })
                .map((t) -> {
                    try {
                        return "spotify:track:" + t.id();   // TODO: Add `uri` field to DTO and use that instead of appending here?
                    } catch (Exception e) {
                        System.out.println("No track found - skipping item again...");
                        return null;
                    }
                })
                .filter(t -> t != null)
                .collect(Collectors.toList());

        AddTracksRequestDTO addTracksReqBody = new AddTracksRequestDTO(uris);

        System.out.println("HIIIIII: " + addTracksReqBody);
        this.addTracksToPlaylist(loifyPlaylistId, addTracksReqBody).block();       // <- TODO: make the playlist dynamic (must come from above )

        // STEP 5: Return the `href` url of the 🍃 playlist - so that users can view in Spotify
         return response;
    }

    private String loifyPlaylistImage(String imageUrl) throws IOException {
        // Download image from the provided URL
        URL url = new URL(imageUrl);
        BufferedImage originalImage = ImageIO.read(url);

        // Resize the image if it exceeds the maximum dimension
        BufferedImage resizedImage = resizeImage(originalImage);

        // Create a new BufferedImage to hold the final image (with emoji overlay)
        BufferedImage finalImage = new BufferedImage(resizedImage.getWidth(), resizedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // Create a graphics object for the final image
        Graphics2D g2d = finalImage.createGraphics();

        // Set the composite to draw the original image at 60% opacity
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g2d.drawImage(resizedImage, 0, 0, null);

        // Reset the composite to draw the emoji with full opacity
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Set font and get the metrics for the emoji size
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 100));
        FontMetrics fontMetrics = g2d.getFontMetrics();
        String emoji = "🍃";

        // Calculate the position to center the emoji
        int stringWidth = fontMetrics.stringWidth(emoji);
        int stringHeight = fontMetrics.getAscent();

        int centerX = (finalImage.getWidth() - stringWidth) / 2;
        int centerY = ((finalImage.getHeight() - stringHeight) / 2) + stringHeight - 10;

        // Draw the emoji in the center of the image
        g2d.drawString(emoji, centerX, centerY);

        g2d.dispose(); // Clean up graphics context

        // Convert the final image with the emoji to Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(finalImage, "png", baos);
        byte[] finalImageBytes = baos.toByteArray();

        // Encode to Base64
        String base64Image = Base64.getEncoder().encodeToString(finalImageBytes);

        // Return the Base64 string to the client
        return base64Image;
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate new dimensions while maintaining aspect ratio
        int newWidth;
        int newHeight;

        if (originalWidth > originalHeight) {
            newWidth = Math.min(originalWidth, 200);
            newHeight = (int) ((double) originalHeight * newWidth / originalWidth);
        } else {
            newHeight = Math.min(originalHeight, 200);
            newWidth = (int) ((double) originalWidth * newHeight / originalHeight);
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    public Mono<String> updatePlaylistImage(String playlistId, String base64Image) {
            return this.webClient.put()
                .uri("v1/playlists/" + playlistId + "/images")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(base64Image)
                .retrieve()
                .bodyToMono(String.class);
    }

        public void resetWebClient() {
            this.webClient = null;
            System.out.println("WebClient has been reset to non-authenticated version.");
        }
}
