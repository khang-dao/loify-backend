package cloud.loify.packages.auth;

import cloud.loify.dto.response.UserDetailsResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final WebClient.Builder webClientBuilder;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String baseUrl;
    public WebClient webClient;

    // Logger instance
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
     *
     * @param principal the authenticated OAuth2User.
     */
    public void handleLogin(@AuthenticationPrincipal OAuth2User principal) {
        // Update all future request headers with Auth Token
        OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient("spotify", principal.getName());

        if (client == null) {
            logger.warn("No authorized client found for user: {}", principal.getName());
            return; // or throw an exception if needed
        }

        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null;

        this.webClient = this.webClientBuilder
                .baseUrl(this.baseUrl)
                .filter(ExchangeFilterFunction.ofRequestProcessor(request -> {
                    ClientRequest updatedRequest = ClientRequest.from(request)
                            .header("Authorization", "Bearer " + accessToken)
                            .build();
                    return Mono.just(updatedRequest);
                }))
                .build();

        logger.info("AUTHENTICATED: Session Created for user: {}", principal.getName());
        logger.debug("Access Token: {}", accessToken);
        if (refreshToken != null) {
            logger.debug("Refresh Token: {}", refreshToken);
        }
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
     *
     * @return Mono containing the UserDetailsResponseDTO.
     */
    public Mono<UserDetailsResponseDTO> getUserProfile() {
        logger.info("Retrieving user profile...");
        return this.webClient.get()
                .uri("/v1/me")
                .retrieve()
                .bodyToMono(UserDetailsResponseDTO.class)
                .doOnSuccess(userDetails -> logger.info("User profile retrieved successfully: {}", userDetails))
                .doOnError(error -> logger.error("Error retrieving user profile: {}", error.getMessage()));
    }
}
