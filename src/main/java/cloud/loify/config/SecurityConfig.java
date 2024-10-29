package cloud.loify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity, adjust as needed for your app
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/auth-check").permitAll()
                        .requestMatchers("/api/spotify/playlists/{playlistId}/tracks/loify").permitAll()    // Temporary allow until POST issue is fixed
                        .requestMatchers("/api/spotify/logout").permitAll()    // Allow access to logout route
                        .requestMatchers("/api/spotify/logout/webclient").permitAll()    // Allow access to logout route

                        // TODO: Delete these
                        .requestMatchers("/api/v1/home").permitAll()
                        .requestMatchers("/api/v1/tracks").permitAll()
                        .requestMatchers("/api/v1/playlists/{playlistId}").permitAll()
                        .requestMatchers("/api/v1/playlists/{username}").permitAll()
                        .requestMatchers("/api/v1/playlists/{playlistId}/tracks").permitAll()
                        .requestMatchers("/api/v1/playlists/{playlistId}/loify").permitAll()
                        .requestMatchers("/api/v1/me/playlists").permitAll()
                        .requestMatchers("/api/v1/me/playlists/loify").permitAll()

                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()    // Allow preflight OPTIONS requests
                        .anyRequest().authenticated() // Require authentication for all other requests
                )
                .oauth2Login(withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/api/spotify/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/api/spotify/logout/webclient")
                        .permitAll()    // Allow everyone to access the logout URL -> see if i can remove the .requestMatchers on `/api/spotify/logout` because I have this alr
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
