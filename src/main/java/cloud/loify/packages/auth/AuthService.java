package cloud.loify.packages.auth;

import cloud.loify.dto.AddTracksRequestDTO;
import cloud.loify.dto.CreatePlaylistRequestDTO;
import cloud.loify.dto.CreatePlaylistResponseDTO;
import cloud.loify.dto.PlaylistDTO;
import cloud.loify.dto.response.PlaylistResponseDTO;
import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.dto.response.UserDetailsResponseDTO;
import cloud.loify.dto.track.TrackItemDTO;
import cloud.loify.dto.track.TrackItemObjectDTO;
import cloud.loify.dto.track.TrackNamesDTO;
import cloud.loify.dto.track.TracksDTO;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class AuthService {

    private final WebClient.Builder webClientBuilder;
    private static final String BASE_URL = "https://api.spotify.com";
    public WebClient webClient;
    public UserDetailsResponseDTO userProfile;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public AuthService(WebClient.Builder webClientBuilder, OAuth2AuthorizedClientService authorizedClientService) {
        this.webClientBuilder = webClientBuilder;
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.authorizedClientService = authorizedClientService;
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

    public void resetWebClient() {
        this.webClient = null;
        System.out.println("WebClient has been reset to non-authenticated version.");
    }

    public Mono<UserDetailsResponseDTO> getUserProfile() {
        return this.webClient.get()
                .uri("/v1/me")
                .retrieve()
                .bodyToMono(UserDetailsResponseDTO.class);
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
}
