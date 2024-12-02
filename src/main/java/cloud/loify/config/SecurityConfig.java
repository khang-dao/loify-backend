package cloud.loify.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for simplicity, adjust as needed for your app
                .authorizeExchange(auth -> auth
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/api/v1/playlists/{playlistId}/loify").permitAll() // Allow preflight OPTIONS requests
                        .pathMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/playlists/{playlistId}/loify").permitAll() // Allow POST requests

                        // TODO: Keep these (remove todo)
                        .pathMatchers("/api/v1/home").permitAll()
                        .pathMatchers("/api/v1/auth/session/check").permitAll()
                        .pathMatchers("/api/v1/auth/session/test").permitAll()

                        // TODO: Delete these
                        .pathMatchers("/api/v1/playlists/{playlistId}/tracks").permitAll()
                        .pathMatchers("/api/v1/me/playlists/loify").permitAll() // For delete loify playlists
                        .anyExchange().authenticated() // Require authentication for all other requests
                )
                .oauth2Login(withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/session/logout")
                        .logoutHandler(new SecurityContextServerLogoutHandler())
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Frontend origin
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allowed methods
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
        configuration.setAllowCredentials(true); // Allow credentials like cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply CORS settings to all paths
        return source;
    }
}
