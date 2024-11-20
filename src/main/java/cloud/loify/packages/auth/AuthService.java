package cloud.loify.packages.auth;

import cloud.loify.packages.me.dto.GetUserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;


@Service
public class AuthService {

    private final WebClient.Builder webClientBuilder;
    public WebClient webClient;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final MusicProviderProperties musicProviderProperties;
    private TokenManager tokenManager;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(WebClient.Builder webClientBuilder, OAuth2AuthorizedClientService authorizedClientService, MusicProviderProperties musicProviderProperties) {
        this.webClientBuilder = webClientBuilder;
//        this.webClient = webClientBuilder.clone().baseUrl(musicProviderProperties.getBaseUrl()).build();
        this.authorizedClientService = authorizedClientService;
        this.musicProviderProperties = musicProviderProperties;
        this.tokenManager = null;
    }

    /**
     * Handles the login process for the authenticated user.
     * @param principal the authenticated OAuth2User.
     */
    public void handleLogin(OAuth2User principal) {
//        OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient("spotify", principal.getName());
//        if (client == null) {
//            logger.warn("No authorized client found for user: {}", principal.getName());
//            return;
//        }

        this.tokenManager = new TokenManager(this.musicProviderProperties, this.authorizedClientService);
        this.webClient = this.webClientBuilder.clone()
            .baseUrl(musicProviderProperties.getBaseUrl())
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(false)))
            .filter(AuthFilter.authFilter(this.tokenManager))
            .build();

        logger.info("AUTHENTICATED: Session Created for user: {}", principal.getName());
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
        if (this.webClient == null) {
            return Mono.error(new IllegalStateException("WebClient is not initialized."));
        }

        return this.webClient.get()
                .uri("/v1/me")
                .retrieve()
                .bodyToMono(GetUserResponseDTO.class)
                .doOnSuccess(userDetails -> logger.info("User profile retrieved successfully: {}", userDetails))
                .doOnError(error -> logger.error("Error retrieving user profile: {}", error.getMessage()));
    }
}
