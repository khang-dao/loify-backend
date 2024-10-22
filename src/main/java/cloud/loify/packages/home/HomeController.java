package cloud.loify.packages.home;


import cloud.loify.dto.*;
import cloud.loify.dto.response.PlaylistResponseDTO;
import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.dto.track.TrackNamesDTO;
import cloud.loify.dto.track.TracksDTO;
import cloud.loify.service.SpotifyService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to the Loify API! Visit the docs for more information :)";
    }
}
