package cloud.loify.packages.track.dto;

import java.util.List;


public record GetTracksFromPlaylistResponseDTO(List<TrackDetailsFromPlaylistDTO> items) {
}
