package cloud.loify.packages.auth;

import cloud.loify.packages.me.dto.GetUserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
public class AuthService {

    private final WebClient.Builder webClientBuilder;
    public WebClient webClient;
    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
    private final MusicProviderProperties musicProviderProperties;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(WebClient webClient, WebClient.Builder webClientBuilder, ReactiveOAuth2AuthorizedClientService reactiveAuthorizedClientService, MusicProviderProperties musicProviderProperties) {
        this.webClient = webClient;
        this.webClientBuilder = webClientBuilder;
        this.authorizedClientService = reactiveAuthorizedClientService;
        this.musicProviderProperties = musicProviderProperties;
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
                            .uri("/v1/me")
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

        // Reactively call the API and fetch user profile
        return this.webClient.get()
                .uri("/v1/me")
                .retrieve()
                .bodyToMono(GetUserResponseDTO.class)
                .doOnSuccess(userDetails -> logger.info("User profile retrieved successfully: {}", userDetails))
                .doOnError(error -> logger.error("Error retrieving user profile: {}", error.getMessage()));
    }
}
