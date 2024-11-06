package cloud.loify.packages.auth;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TokenManager {

    private final MusicProviderProperties musicProviderProperties;
    private String accessToken;
    private final String refreshToken;

    public TokenManager(MusicProviderProperties musicProviderProperties, OAuth2AuthorizedClient authClient) {
        this.musicProviderProperties = musicProviderProperties;
        this.accessToken = authClient.getAccessToken().getTokenValue();
        this.refreshToken = authClient.getRefreshToken().getTokenValue();
    }

    public synchronized String getAccessToken() {
        return this.accessToken;
    }

    public synchronized Boolean hasRefreshToken() {
        return this.refreshToken != null && !this.refreshToken.isEmpty();
    }

    public synchronized void refreshAccessToken() {
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString((musicProviderProperties.getClientId() + ":" + musicProviderProperties.getClientSecret()).getBytes(StandardCharsets.UTF_8));
        System.out.println("BASIC AUTH: " + basicAuth);

        WebClient.builder()
            .baseUrl("https://accounts.spotify.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .defaultHeader("Authorization", basicAuth) // Add the Authorization header
            .build()
                .post()
                .uri("/api/token")
                .bodyValue("grant_type=refresh_token&refresh_token=" + this.refreshToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> response.get("access_token").asText())
                .doOnNext(token -> {
                    System.out.println("RADICALLLL ACCESS TOKENNNNNNNNN: " + token);
                    this.accessToken = token;
                })
                .subscribe();
    }
}