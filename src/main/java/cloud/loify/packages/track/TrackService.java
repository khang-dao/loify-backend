package cloud.loify.packages.track;

import cloud.loify.dto.response.TrackSearchResponseDTO;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TrackService {

//    public Mono<TrackSearchResponseDTO> getFirstTrackByTrackName(String trackName) {
//        try {
//            return this.webClient.get()
//                    .uri("/v1/search?q=track:" + trackName + "&type=track&limit=1")
//                    .retrieve()
//                    .bodyToMono(TrackSearchResponseDTO.class);
//        }
//        catch (JSONException err) {
//            System.out.println("Song could not be Loify-ed: " + err);
//        }
//        return null;
//    }
}
