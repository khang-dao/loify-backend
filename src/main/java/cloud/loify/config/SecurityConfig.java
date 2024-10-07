package cloud.loify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean // Ensure this method is marked with @Bean so Spring recognizes it as a bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Apply CORS settings
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity, adjust as needed for your app
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()    // Allow harmless home route
                        .requestMatchers("/api/auth-check").permitAll()    // Allow for auth check
                        .requestMatchers("/api/spotify/playlists/{playlistId}/tracks/loify").permitAll()    // Temporary allow until POST issue is fixed
                        .requestMatchers("/api/spotify/logout").permitAll()    // Allow access to logout route
                        .requestMatchers("/api/spotify/logout/webclient").permitAll()    // Allow access to logout route
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()    // Allow preflight OPTIONS requests
                        .anyRequest().authenticated() // Require authentication for all other requests
                )
                .oauth2Login(withDefaults()) // OAuth2 login configuration
                .logout(logout -> logout
                        .logoutUrl("/api/spotify/logout") // Specify the logout URL
                        .invalidateHttpSession(true)      // Invalidate the session on logout
                        .clearAuthentication(true)        // Clear the authentication on logout
                        .deleteCookies("JSESSIONID")      // Remove session cookies (optional)
                        .logoutSuccessUrl("/api/spotify/logout/webclient") // Redirect after successful logout (adjust URL as needed)
                        .permitAll()                      // Allow everyone to access the logout URL
                )
                .build(); // Build the security filter chain
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
