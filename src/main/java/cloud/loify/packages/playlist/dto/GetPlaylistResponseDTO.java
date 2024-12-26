package cloud.loify.packages.playlist.dto;

import cloud.loify.packages.common.dto.CoverImageDetailsDTO;
import cloud.loify.packages.track.dto.GetTracksFromPlaylistResponseDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GetPlaylistResponseDTO(
        String id,
        String name,
        GetTracksFromPlaylistResponseDTO tracks,
        CoverImageDetailsDTO image
) {
    @JsonCreator
    public GetPlaylistResponseDTO(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("tracks") GetTracksFromPlaylistResponseDTO tracks,
            @JsonProperty("images") List<CoverImageDetailsDTO> images
    ) {
        this(
                id,
                name,
                tracks,
                extractFirstImage(images)
        );
    }

    private static CoverImageDetailsDTO extractFirstImage(List<CoverImageDetailsDTO> images) {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }
}
