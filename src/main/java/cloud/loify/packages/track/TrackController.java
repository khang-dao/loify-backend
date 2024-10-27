package cloud.loify.packages.track;

import cloud.loify.dto.response.TrackSearchResponseDTO;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping // Example URL: http://loify.com/api/v1/tracks?trackName=hotline+bling
    public Mono<TrackSearchResponseDTO> getFirstTrackByTrackName(@RequestParam String trackName) {
        return trackService.getFirstTrackByTrackName(trackName);
    }
}
