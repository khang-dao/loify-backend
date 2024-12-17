package cloud.loify.packages.auth;

import cloud.loify.packages.me.dto.GetUserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
    public WebClient webClient;

    public AuthService(ReactiveOAuth2AuthorizedClientService reactiveAuthorizedClientService, WebClient webClient) {
        this.authorizedClientService = reactiveAuthorizedClientService;
        this.webClient = webClient;
    }

    /**
     * Handles the login process for the authenticated user.
     * @param principal the authenticated OAuth2User.
     */
    public Mono<Void> handleLogin(String principalName) {
        return authorizedClientService.loadAuthorizedClient("spotify", principalName)
                .flatMap(authorizedClient -> {
                    logger.info("REACHED;");
                    return webClient.get()
                            .uri("/me")
                            .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
                            .retrieve()
                            .bodyToMono(Void.class)
                            .doOnSuccess(response ->
                                    System.out.println("Authenticated and session created for: " + principalName)
                            );
                });
    }

    /**
     * Resets the WebClient to a non-authenticated version.
     */
//    public Mono<Void> resetWebClient() {
//        return Mono.fromRunnable(() -> {
//            this.webClient = null;
//            logger.info("WebClient has been reset to non-authenticated version.");
//        });
//    }

    /**
     * Retrieves the user profile from the API.
     * @return Mono containing the GetUserResponseDTO.
     */
    public Mono<GetUserResponseDTO> getUserProfile() {
        logger.info("Retrieving user profile...");
        if (this.webClient == null) {
            return Mono.error(new IllegalStateException("WebClient is not initialized.")); // Fail fast if WebClient is missing
        }

        return this.webClient.get()
                .uri("/me")
                .retrieve()
                .bodyToMono(GetUserResponseDTO.class)
                .doOnSuccess(userDetails -> logger.info("User profile retrieved successfully: {}", userDetails))
                .doOnError(error -> logger.error("Error retrieving user profile: {}", error.getMessage()));
    }
}
