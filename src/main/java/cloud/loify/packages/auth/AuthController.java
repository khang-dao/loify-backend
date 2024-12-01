package cloud.loify.packages.auth;

import org.springframework.http.server.reactive.ServerHttpResponse;
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
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${frontend.url}") private String frontendUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
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

    /**
     * Creates a new session (login) for the authenticated user.
     *
     * @param principal The authenticated OAuth2 user.
     * @param response The HttpServletResponse used for redirection.
     * @return ResponseEntity<Void> indicating the result of the login attempt.
     *         Returns 302 FOUND if successful; 401 UNAUTHORIZED if principal is null;
     *         500 INTERNAL_SERVER_ERROR if an exception occurs.
     */
    @GetMapping("/session")
    public Mono<Void> createSession(@AuthenticationPrincipal OAuth2User principal, ServerWebExchange exchange) {
        if (principal == null) {
            return Mono.error(new IllegalArgumentException("Principal cannot be null."));
        }
        logger.info("HELLO WORKLD");

        return authService.handleLogin(principal.getName())
                .then(Mono.fromRunnable(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().setLocation(URI.create("http://localhost:3000/loify"));
                }));
    }

    /**
     * Logs out the user by invalidating the current session.
     *
     * @return ResponseEntity<Void> indicating the result of the logout attempt.
     *         Returns 204 NO CONTENT if successful; 500 INTERNAL_SERVER_ERROR if an exception occurs.
     */
    @GetMapping("/session/logout")
    public Mono<Void> deleteSession(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        try {
//            authService.resetWebClient();
            logger.info("User session invalidated successfully");

            // Set the Location header for redirection
            response.getHeaders().setLocation(URI.create("http://localhost:3000"));
            response.setStatusCode(HttpStatus.FOUND); // 302 Redirect
            return response.setComplete(); // Complete the response

        } catch (Exception e) {
            logger.error("Error during logout process: {}", e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete(); // Complete the response with an error status
        }
    }
}
