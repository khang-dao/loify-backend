package cloud.loify.packages.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Controller for handling authentication-related operations.
 */
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${frontend.url}") private String frontendUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Initiates a new session (login) for the authenticated user.
     *
     * @param principal The authenticated OAuth2 user, provided by Spring Security.
     * @param exchange The server exchange for handling HTTP requests and responses.
     * @return A {@link Mono<Void>} indicating the outcome of the login attempt.
     *         Returns:
     *         - 302 FOUND if the login is successful and the session is created.
     *         - 401 UNAUTHORIZED if the principal is null or not authenticated.
     *         - 500 INTERNAL_SERVER_ERROR if an unexpected error occurs during processing.
     * @throws IllegalArgumentException if the principal is null.
     */
    @GetMapping("/session")
    public Mono<Void> createSession(@AuthenticationPrincipal OAuth2User principal, ServerWebExchange exchange) {
        if (principal == null) {
            return Mono.error(new IllegalArgumentException("Principal cannot be null."));
        }

        return authService.handleLogin(principal.getName())
                .then(Mono.fromRunnable(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().setLocation(URI.create(frontendUrl + "/loify"));
                }));
    }

    /**
     * Checks if the user is logged in by retrieving their profile.
     *
     * @return Mono<ResponseEntity<String>> containing the login status message.
     * If the user is logged in, returns 200 OK; otherwise, returns 401 UNAUTHORIZED.
     */
    @GetMapping("/session/check")
    public Mono<ResponseEntity<String>> getSessionStatus() {
        return authService.getUserProfile()
                .map(user -> {
                    logger.info("User is logged in: {}", user.id());
                    return ResponseEntity.ok("User is logged in.");
                })
                .onErrorResume(e -> {
                    logger.error("Failed to retrieve user profile: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("UNAUTHORIZED: Invalid token or session expired"));
                });
    }
}
