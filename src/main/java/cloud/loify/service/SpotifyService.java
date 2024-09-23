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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class SpotifyService {

    private final WebClient.Builder webClientBuilder;
    private static final String BASE_URL = "https://api.spotify.com";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    @Value("${spotify.client_id}")
    private String clientId;
    @Value("${spotify.client_secret}")
    private String clientSecret;
    @Value("${spotify.grant_type}")
    private String grantType;
    @Value("${spotify.scopes}")
    private String scopes;
    @Value("${spotify.redirect_uri}")
    private String redirectUri;

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

    public void updateRequestHeadersWithAuthToken(@AuthenticationPrincipal OAuth2User principal) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("spotify", principal.getName());
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

    public Mono<PlaylistResponseDTO> getPlaylist(String playlistId) {
        return this.webClient.get()
                .uri("v1/playlists/" + playlistId)
                .retrieve()
                .bodyToMono(PlaylistResponseDTO.class);

    }

    public void createPlaylist(String username, CreatePlaylistRequestDTO requestBody) {
        CreatePlaylistResponseDTO responseBody = this.webClient.post()
                .uri("v1/users/" + username + "/playlists")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(CreatePlaylistResponseDTO.class)
                .block();

        System.out.println("Playlist Created: " + responseBody);
    }

    public void addTracksToPlaylist(String playlistId, AddTracksRequestDTO requestBody) {
        System.out.println(requestBody);
        String responseBody = this.webClient.post()
                .uri("v1/playlists/" + playlistId + "/tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Tracks added to Playlist !");
    }

    private String loifyPlaylistName(String playlistName){
        return "loify - " + playlistName + " 🍃";
    }

    private String loifyPlaylistDescription(String playlistName){
        return "a loify-ed version of playlist " + playlistName + " 🍃";
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

    public void createLoifyedPlaylistAndAddLoifyedTracks(@PathVariable String playlistId) {
        // STEP 0: Get current playlist details

        // STEP 1: Create new (empty 🍃) playlist
//        String currentPlaylistName = this.getPlaylist(playlistId).block().name();
////        String loifyPlaylistName = this.loifyPlaylistName(currentPlaylistName);
////        String loifyPlaylistDescription = this.loifyPlaylistDescription(currentPlaylistName);
////
////        CreatePlaylistRequestDTO createPlaylistReqBody = new CreatePlaylistRequestDTO(loifyPlaylistName, loifyPlaylistDescription, true, true);
//        this.createPlaylist(this.userProfile.id(), createPlaylistReqBody);


        // STEP 2: Loify all tracks in playlist -> return an array of 🍃tracks -> Flux<TrackSearchResponseDTO>.block()
        List<TrackSearchResponseDTO> loifyedTracks = this.getAndLoifyAllTracksInPlaylist(playlistId).collectList().block();
        System.out.println(loifyedTracks);


        // STEP 3: Add loify-ed tracks -> to (empty 🍃) playlist
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
                        return "spotify:track:" + t.id();
                    } catch (Exception e) {
                        System.out.println("No track found - skipping item again...");
                        return null;
                    }
                })
                .filter(t -> t != null)
                .collect(Collectors.toList());

        AddTracksRequestDTO addTracksReqBody = new AddTracksRequestDTO(uris);

        System.out.println("HIIIIII: " + addTracksReqBody);
        this.addTracksToPlaylist("0jrfwoUkUFfjOfEbNnhIgs", addTracksReqBody);       // <- TODO: make the playlist dynamic (must come from above )


        // STEP 4: Return the `href` url of the 🍃 playlist - so that users can view in Spotify
        // return x.href
    }

    public void addCustomImageToPlaylist(String userId) {}
}
