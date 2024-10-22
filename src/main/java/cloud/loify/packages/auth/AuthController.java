package cloud.loify.packages.auth;

import cloud.loify.packages.playlist.PlaylistService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Isn't this weird cause this is the login route AND callback? (possibly we dont need a login route... becquse all routes require login)
    @GetMapping("/login")
    public ResponseEntity<Void> loginCallback(@AuthenticationPrincipal OAuth2User principal, HttpServletResponse response) {
        authService.updateRequestHeadersWithAuthToken(principal);
//        authService.setUserProfile(); TODO: Do I need this?

        // Redirect to frontend app
        response.setStatus(HttpServletResponse.SC_FOUND); // HTTP 302
        response.setHeader("Location", "http://localhost:3000/loify"); // Replace with your actual frontend URL
        return new ResponseEntity<>(HttpStatus.FOUND);
    }

    @GetMapping("/check")
    public Mono<ResponseEntity<String>> isLoggedIn() {
        return authService.getUserProfile()
                .map(user -> ResponseEntity.ok("User is logged in."))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("UNAUTHORIZED: Invalid token or session expired"))); // If there's an error (e.g., token invalid), return UNAUTHORIZED
    }

    @GetMapping("/logout/webclient")
    public ResponseEntity<Void> logout() {
        authService.resetWebClient(); // Call the service method to invalidate the token
        return ResponseEntity.noContent().build(); // Return 204 No Content response
    }
}
