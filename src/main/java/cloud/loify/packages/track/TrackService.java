package cloud.loify.packages.track;

import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.packages.auth.AuthService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TrackService {

    private final AuthService auth;

    public TrackService(AuthService authService) {
        this.auth = authService;
    }

    public Mono<TrackSearchResponseDTO> getFirstTrackByTrackName(String trackName) {
        return this.auth.webClient.get()
                .uri("/v1/search?q=track:" + trackName + "&type=track&limit=1")
                .retrieve()
                .bodyToMono(TrackSearchResponseDTO.class)
                .onErrorResume(err -> {
                    System.out.println("Song could not be Loify-ed: " + err);
                    return Mono.empty(); // or return a fallback response
                });
    }

}
