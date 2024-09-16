package cloud.loify.service;

import cloud.loify.dto.*;
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
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private String authToken;
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

    public List<String>  getAllTrackNamesInPlaylist(String playlistId) {
        JSONArray tracks =  this.webClient.get()
                .uri("/v1/playlists/" + playlistId)
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
        return "loify - " + playlistName;
    }

    private String loifyTrackName(String trackName){
        return trackName + " lofi";
    }

    public List<String> getLoifyedTracks(TrackNamesDTO requestBody) {
        List<String> trackNames = requestBody.trackNames();
        Stream<String> loifyedTrackNames = trackNames.stream().map(this::loifyTrackName);
        return loifyedTrackNames
                .map(this::getFirstTrackIdByTrackName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public void addCustomImageToPlaylist(String userId) {

    }
}
