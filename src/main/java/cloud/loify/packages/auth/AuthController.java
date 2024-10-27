package cloud.loify.packages.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import reactor.core.publisher.Mono;

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
    public ResponseEntity<Void> createSession(@AuthenticationPrincipal OAuth2User principal, HttpServletResponse response) {
        try {
            if (principal == null) {
                logger.warn("Attempted login with null principal");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            authService.handleLogin(principal);
            logger.info("User [{}] has successfully logged in", principal.getName());

            // Redirect to frontend app
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", this.frontendUrl); // Replace with actual frontend URL
            return new ResponseEntity<>(HttpStatus.FOUND);

        } catch (Exception e) {
            logger.error("Error during login process: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Logs out the user by invalidating the current session.
     *
     * @return ResponseEntity<Void> indicating the result of the logout attempt.
     *         Returns 204 NO CONTENT if successful; 500 INTERNAL_SERVER_ERROR if an exception occurs.
     */
    @DeleteMapping("/session")
    public ResponseEntity<Void> deleteSession() {
        try {
            authService.resetWebClient();
            logger.info("User session invalidated successfully");
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Error during logout process: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
