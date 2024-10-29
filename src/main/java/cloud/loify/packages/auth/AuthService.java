package cloud.loify.packages.auth;

import cloud.loify.packages.me.dto.GetUserResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;


@Service
public class AuthService {

    private final WebClient.Builder webClientBuilder;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String baseUrl;
    public WebClient webClient;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(WebClient.Builder webClientBuilder, OAuth2AuthorizedClientService authorizedClientService,
                       @Value("${api.base.url}") String baseUrl) {
        this.webClientBuilder = webClientBuilder;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.authorizedClientService = authorizedClientService;
        this.baseUrl = baseUrl;
    }

    /**
     * Handles the login process for the authenticated user.
     * @param principal the authenticated OAuth2User.
     */
    public void handleLogin(@AuthenticationPrincipal OAuth2User principal) {
        OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient("spotify", principal.getName());
        if (client == null) {
            logger.warn("No authorized client found for user: {}", principal.getName());
            return;
        }

        String initialAccessToken = client.getAccessToken().getTokenValue();
        String refreshToken = client.getRefreshToken().getTokenValue();

        this.webClient = this.webClientBuilder
                .baseUrl(this.baseUrl)
                .filter((request, next) -> next.exchange(
                                ClientRequest.from(request)
                                        .header("Authorization", "Bearer " + initialAccessToken)
                                        .build())
                        .flatMap(response -> {
                            if (response.statusCode() == HttpStatus.UNAUTHORIZED && refreshToken != null) {
                                // Refresh the token on a 401 Unauthorized response
                                return refreshAccessToken(client)
                                        .flatMap(newAccessToken -> {
                                            // Update the WebClient's authorization header with the new access token
                                            this.webClient = this.webClientBuilder
                                                    .baseUrl(this.baseUrl)
                                                    .filter(ExchangeFilterFunction.ofRequestProcessor(req -> Mono.just(
                                                            ClientRequest.from(req)
                                                                    .header("Authorization", "Bearer " + newAccessToken)
                                                                    .build()
                                                    )))
                                                    .build();

                                            // Retry the original request with the new access token
                                            logger.info("New access token generated using refresh token");
                                            return next.exchange(ClientRequest.from(request)
                                                    .header("Authorization", "Bearer " + newAccessToken)
                                                    .build());
                                        });
                            }
                            return Mono.just(response); // Proceed if no 401 Unauthorized or no refresh token available
                        }))
                .build();
        logger.info("AUTHENTICATED: Session Created for user: {}", principal.getName());
    }

    /**
     * Refreshes the access token using the provided OAuth2AuthorizedClient's refresh token.
     * This method sends a POST request to the OAuth2 token endpoint to obtain a new access token.
     *
     * @param client The {@link OAuth2AuthorizedClient} containing the refresh token.
     * @return A {@link Mono<String>} that emits the new access token if successful, or completes empty if the refresh fails.
     * @throws IllegalArgumentException if the client or refresh token is null.
     */
    private Mono<String> refreshAccessToken(OAuth2AuthorizedClient client) {
        String refreshToken = client.getRefreshToken().getTokenValue();

        return this.webClientBuilder
                .baseUrl(this.baseUrl)
                .build()
                .post()
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> response.get("access_token").asText());
    }


    /**
     * Resets the WebClient to a non-authenticated version.
     */
    public void resetWebClient() {
        this.webClient = null;
        logger.info("WebClient has been reset to non-authenticated version.");
    }

    /**
     * Retrieves the user profile from the API.
     * @return Mono containing the UserDetailsResponseDTO.
     */
    public Mono<GetUserResponseDTO> getUserProfile() {
        logger.info("Retrieving user profile...");
        return this.webClient.get()
                .uri("/v1/me")
                .retrieve()
                .bodyToMono(GetUserResponseDTO.class)
                .doOnSuccess(userDetails -> logger.info("User profile retrieved successfully: {}", userDetails))
                .doOnError(error -> logger.error("Error retrieving user profile: {}", error.getMessage()));
    }
}
