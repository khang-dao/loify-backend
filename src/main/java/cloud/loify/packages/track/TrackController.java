package cloud.loify.packages.track;

import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.packages.track.exceptions.TrackNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * Retrieves the first track matching the specified track name.
     *
     * @param trackName the name of the track to search for.
     * @return a Mono containing the TrackSearchResponseDTO with the track details.
     * @throws ResponseStatusException if the track is not found or an error occurs during the search.
     */
    @GetMapping // Example URL: http://loify.com/api/v1/tracks?trackName=hotline+bling
    public Mono<ResponseEntity<TrackSearchResponseDTO>> getFirstTrackByTrackName(@RequestParam String trackName) {
        // Validate the input parameter
        if (trackName == null || trackName.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        return trackService.getFirstTrackByTrackName(trackName)
                .map(track -> ResponseEntity.ok(track))
                .switchIfEmpty(Mono.error(new TrackNotFoundException("Track not found: " + trackName)))
                .onErrorResume(error -> {
                    // Log the error and return a server error response
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving track", error));
                });
    }
}
