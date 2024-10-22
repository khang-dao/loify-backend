package cloud.loify.packages.playlist;

import cloud.loify.dto.AddTracksRequestDTO;
import cloud.loify.dto.CreatePlaylistRequestDTO;
import cloud.loify.dto.CreatePlaylistResponseDTO;
import cloud.loify.dto.PlaylistDTO;
import cloud.loify.dto.response.PlaylistResponseDTO;
import cloud.loify.dto.response.TrackSearchResponseDTO;
import cloud.loify.dto.response.UserDetailsResponseDTO;
import cloud.loify.dto.track.TrackItemDTO;
import cloud.loify.dto.track.TrackItemObjectDTO;
import cloud.loify.dto.track.TracksDTO;
import cloud.loify.packages.auth.AuthService;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PlaylistService {

    private WebClient webClient;
    private UserDetailsResponseDTO userProfile;

    public PlaylistService(AuthService authService) {
        this.webClient = authService.webClient;
        this.userProfile = authService.userProfile;
    }


    public Mono<PlaylistResponseDTO> getPlaylistById(String playlistId) {
        return this.webClient.get()
                .uri("v1/playlists/" + playlistId)
                .retrieve()
                .bodyToMono(PlaylistResponseDTO.class);
    }

//    public Mono<TracksDTO> getAllTracksInPlaylist(String playlistId) {
//        return this.webClient.get()
//                .uri("/v1/playlists/" + playlistId + "/tracks")
//                .retrieve()
//                .bodyToMono(TracksDTO.class);
//    }
//
//    public Mono<CreatePlaylistResponseDTO> createPlaylist(String username, CreatePlaylistRequestDTO requestBody) {
//        return this.webClient.post()
//                .uri("v1/users/" + username + "/playlists")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(CreatePlaylistResponseDTO.class);
//    }
//
//    public Mono<String> addTracksToPlaylist(String playlistId, AddTracksRequestDTO requestBody) {
//        System.out.println(requestBody);
//        System.out.println("Adding tracks to playlist...");
//        return this.webClient.post()
//                .uri("v1/playlists/" + playlistId + "/tracks")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(String.class);  // NOTE: String = `snapshot_id`
//    }
//
//
//
//
//    public Flux<TrackSearchResponseDTO> getAndLoifyAllTracksInPlaylist(String playlistId) {
////    public void getAndLoifyAllTracksInPlaylist(String playlistId) {
//        // STEP 1: Get all `tracks` in playlist --- (maybe just call `getAllTracksInPlaylist()`)
//        TracksDTO tracks = this.getAllTracksInPlaylist(playlistId).block();
//
//        // STEP 2: Extract all `tracksNames` in playlist
//        Stream<String> trackNames = tracks.items()
//                .stream()
//                .map((t) -> (TrackItemDTO) t)
//                .map((t) -> t.track().name());
//
//        // STEP 3: "loify" all `trackNames` - `loifyedTrackNames = trackNames.map(this::loifyTrackName)`
//        Stream<String> loifyedTrackNames = trackNames.map(this::loifyTrackName);
//
//        // STEP 3: Get all "loify-ed" tracks (by using FIRST_TRACK algo) - `loifedTracks = loifyedTrackNames.map(this::getFirstTrackByTrackName)`
//        Flux<TrackSearchResponseDTO> loifyedTracks = Flux.fromStream(loifyedTrackNames
//                        .map(this::getFirstTrackByTrackName))
//                .flatMap(mono -> mono);  // Flatten the Mono<Track> into a Flux<Track>
//
//        // STEP 4: Now we have all Mono<TrackDTO> `loifyedTracks`, simply return - `return loifyedTracks`
//        return loifyedTracks;
//
//    }
//
//    // TODO: Make this atomic - because sometimes the playlist is created, but the songs aren't added
//    // TODO: ^ need to make it so that if one fails, then it's as if the method was never called
//    // TODO: ^ (might involve deleting the playlist if the transaction fails)
//    public CreatePlaylistResponseDTO createLoifyedPlaylistAndAddLoifyedTracks(@PathVariable String playlistId) {
//        // STEP 0: Get current playlist details
//        // STEP 1: Create new (empty 🍃) playlist
//        PlaylistResponseDTO currentPlaylist = this.getPlaylistById(playlistId).block();
//        String currentPlaylistName = currentPlaylist.name();
//        String loifyPlaylistName = this.loifyPlaylistName(currentPlaylistName);
//        String loifyPlaylistDescription = this.loifyPlaylistDescription(currentPlaylistName);
//
//        // TODO: Check if playlist exists already
//        CreatePlaylistRequestDTO createPlaylistReqBody = new CreatePlaylistRequestDTO(loifyPlaylistName, loifyPlaylistDescription, true, true);
//
//        CreatePlaylistResponseDTO response = this.createPlaylist(this.userProfile.id(), createPlaylistReqBody).block();
//        String loifyPlaylistId = response.id();
//
//        // STEP 2: Update (empty 🍃) playlist - image
//        try {
//            String currentPlaylistImage = currentPlaylist.images().get(0).url();
//            String loifyPlaylistImage = this.loifyPlaylistImage(currentPlaylistImage);
//            this.updatePlaylistImage(loifyPlaylistId, loifyPlaylistImage).block();
//        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        catch (Exception e) {
//            // TODO: Implement default cover image
//            System.out.println("Original Playlist does not have cover image... fallback to default loify image");
//        }
//
//
//        // STEP 3: Loify all tracks in playlist -> return an array of 🍃tracks -> Flux<TrackSearchResponseDTO>.block()
//        List<TrackSearchResponseDTO> loifyedTracks = this.getAndLoifyAllTracksInPlaylist(playlistId).collectList().block();
//        System.out.println(loifyedTracks);
//
//        // STEP 4: Add loify-ed tracks -> to (empty 🍃) playlist
//        List<String> uris = loifyedTracks
//                .stream()
//                .map((t) -> {
//                    try {
//                        return (TrackItemObjectDTO) t.tracks().items().get(0);
//                    } catch (Exception e) {
//                        System.out.println("No track found - skipping item...");
//                        return null;
//                    }
//                })
//                .map((t) -> {
//                    try {
//                        return "spotify:track:" + t.id();   // TODO: Add `uri` field to DTO and use that instead of appending here?
//                    } catch (Exception e) {
//                        System.out.println("No track found - skipping item again...");
//                        return null;
//                    }
//                })
//                .filter(t -> t != null)
//                .collect(Collectors.toList());
//
//        AddTracksRequestDTO addTracksReqBody = new AddTracksRequestDTO(uris);
//
//        System.out.println("HIIIIII: " + addTracksReqBody);
//        this.addTracksToPlaylist(loifyPlaylistId, addTracksReqBody).block();       // <- TODO: make the playlist dynamic (must come from above )
//
//        // STEP 5: Return the `href` url of the 🍃 playlist - so that users can view in Spotify
//        return response;
//    }
//
//
//    public Mono<String> updatePlaylistImage(String playlistId, String base64Image) {
//        return this.webClient.put()
//                .uri("v1/playlists/" + playlistId + "/images")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(base64Image)
//                .retrieve()
//                .bodyToMono(String.class);
//    }
}
