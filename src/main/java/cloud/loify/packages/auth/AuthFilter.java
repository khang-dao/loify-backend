package cloud.loify.packages.auth;

import cloud.loify.packages.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public class AuthFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    static ExchangeFilterFunction authFilter(TokenManager tokenManager) {
        System.out.println("AUTH FILTER TRIGGERED");

        return (request, next) -> ApiUtils.retryWithDelay(() -> next.exchange(ClientRequest.from(request)
            .header("Authorization", "Bearer " + tokenManager.getAccessToken())
            .build())
//            .flatMap(response -> {
//                if ((response.statusCode() == HttpStatus.UNAUTHORIZED || response.statusCode() == HttpStatus.FORBIDDEN || response.statusCode().is3xxRedirection()) && tokenManager.hasRefreshToken()) {
//
//                    tokenManager.refreshAccessToken();
//                    return next.exchange(ClientRequest.from(request)
//                        .header("Authorization", "Bearer " + tokenManager.getAccessToken())
//                        .build());
//                } else if (response.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
//                    logger.warn("Received 429 Too Many Requests. Retrying...");
//                    return Mono.error(new WebClientResponseException(
//                            response.statusCode().value(),
//                            "Received 429 Too Many Requests",
//                            response.headers().asHttpHeaders(),
//                            null,
//                            null));
//                }
//                return Mono.just(response); // Proceed with the original response if no special handling is needed
//            })
        );
    }
}
