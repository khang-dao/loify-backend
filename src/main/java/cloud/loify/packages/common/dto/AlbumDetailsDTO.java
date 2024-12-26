package cloud.loify.packages.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AlbumDetailsDTO(
        String id,
        String name,
        CoverImageDetailsDTO image
) {
    @JsonCreator
    public AlbumDetailsDTO(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("images") List<CoverImageDetailsDTO> images
    ) {
        this(
                id,
                name,
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
