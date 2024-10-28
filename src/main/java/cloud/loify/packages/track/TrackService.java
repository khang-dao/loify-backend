package cloud.loify.packages.track;

import cloud.loify.packages.track.dto.SearchTrackResponseDTO;
import cloud.loify.packages.auth.AuthService;
import cloud.loify.packages.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TrackService {

    private final AuthService auth;

    private static final Logger logger = LoggerFactory.getLogger(TrackService.class);

    public TrackService(AuthService authService) {
        this.auth = authService;
    }

    /**
     * Retrieves the first track that matches the specified track name.
     *
     * @param trackName the name of the track to search for.
     * @return a Mono containing the TrackSearchResponseDTO with the track details.
     */
    public Mono<SearchTrackResponseDTO> getFirstTrackByTrackName(String trackName) {
        logger.info("Searching for track with name: {}", trackName);

        return ApiUtils.retryWithDelay(() ->
                this.auth.webClient.get()
                        .uri("/v1/search?q=track:" + trackName + "&type=track&limit=1")
                        .retrieve()
                        .bodyToMono(SearchTrackResponseDTO.class)
                        .doOnSuccess(track -> logger.info("Track found: {}", track))
                        .doOnError(err -> logger.error("Error retrieving track: {}", err.getMessage()))
                )
                        .onErrorResume(err -> {
                            logger.warn("Song could not be Loify-ed: {}", err.getMessage());
                            return Mono.empty(); // or return a fallback response
                        });
    }
}