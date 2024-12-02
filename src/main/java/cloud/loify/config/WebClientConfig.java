package cloud.loify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2FilterFunction = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2FilterFunction.setDefaultOAuth2AuthorizedClient(true); // Use default client if none is specified

        return WebClient.builder()
                .baseUrl("https://api.spotify.com/v1")
                .filter(oauth2FilterFunction) // Apply the OAuth2 filter
                .filter(retryFilter()) // Apply the retry filter for 429 responses
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16 MB
                .build();
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService) {

        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);

        manager.setAuthorizedClientProvider(
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .password()
                        .build()
        );

        return manager;
    }

    private ExchangeFilterFunction retryFilter() {
        return (request, next) -> next.exchange(request)
                .flatMap(response -> {
                    // Retry only if the response status is 429
                    if (response.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        String retryAfterHeader = response.headers().asHttpHeaders().getFirst("Retry-After");
                        long retryAfterSeconds = retryAfterHeader != null ? Long.parseLong(retryAfterHeader) : 1;

                        System.out.printf("Received 429 Too Many Requests. Retrying after %d seconds.%n", retryAfterSeconds);

                        // Delay before retrying the operation
                        return Mono.delay(Duration.ofSeconds(retryAfterSeconds))
                                .then(next.exchange(request));
                    }
                    return Mono.just(response);
                });
    }
}
