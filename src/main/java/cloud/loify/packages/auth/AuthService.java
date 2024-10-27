package cloud.loify.packages.auth;

import cloud.loify.dto.response.UserDetailsResponseDTO;
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

    public AuthService(WebClient.Builder webClientBuilder, OAuth2AuthorizedClientService authorizedClientService, @Value("${api.base.url}") String baseUrl) {
        this.webClientBuilder = webClientBuilder;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.authorizedClientService = authorizedClientService;
        this.baseUrl = baseUrl;
    }

    public void handleLogin(@AuthenticationPrincipal OAuth2User principal) {
        // Update all future request headers with Auth Token
        OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient("spotify", principal.getName());
        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = client.getRefreshToken().getTokenValue();

        this.webClient = this.webClientBuilder
                .baseUrl(this.baseUrl)
                .filter(ExchangeFilterFunction.ofRequestProcessor(request -> {
                    ClientRequest updatedRequest = ClientRequest.from(request)
                            .header("Authorization", "Bearer " + accessToken)
                            .build();
                    return Mono.just(updatedRequest);
                }))
                .build();
        System.out.println("AUTHENTICATED: Session Created");
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
}
