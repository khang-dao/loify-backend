package cloud.loify.packages.track;

import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.dto.track.TrackNamesDTO;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracks")
public class TrackController {

//    @GetMapping("/api/spotify/tracks/{trackName}")
//    public Mono<TrackSearchResponseDTO> getFirstTrackByTrackName(@PathVariable String trackName) {
//        return spotifyService.getFirstTrackByTrackName(trackName);
//    }
}
