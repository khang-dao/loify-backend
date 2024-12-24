package cloud.loify.packages.playlist.dto;

import java.util.List;

public record AddTracksToPlaylistRequestDTO(List<String> uris) {
}
