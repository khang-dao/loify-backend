package cloud.loify.packages.track;

import cloud.loify.packages.track.dto.SearchTrackResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TrackService {

    private static final Logger logger = LoggerFactory.getLogger(TrackService.class);
    private final WebClient webClient;

    public TrackService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Retrieves the first track that matches the specified track name.
     *
     * @param trackName the name of the track to search for.
     * @return a Mono containing the TrackSearchResponseDTO with the track details.
     */
    public Mono<SearchTrackResponseDTO> getFirstTrackByTrackName(String trackName) {
        logger.info("Searching for track with name: {}", trackName);
        return this.webClient.get()
                .uri("/search?q=track:" + trackName + "&type=track&limit=1")
                .retrieve()
                .bodyToMono(SearchTrackResponseDTO.class)
                .doOnSuccess(track -> {
                    if (track != null && !track.tracks().items().isEmpty()) {
                        logger.info("Track found: {}", track.tracks().items().get(0));
                    } else {
                        logger.info("No track found with name: {}", trackName);
                    }
                })
                .doOnError(err -> logger.error("Song could not be Loify-ed: {}", err.getMessage()));
    }
}
