package cloud.loify.packages.playlist.dto;


import cloud.loify.packages.common.dto.CoverImageDetailsDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record CreatePlaylistResponseDTO(
        String id,
        String name,
        String description,
        CoverImageDetailsDTO image,
        String url,
        @JsonProperty("public") boolean isPublic,
        @JsonProperty("collaborative") boolean isCollaborative
) {
    @JsonCreator
    public CreatePlaylistResponseDTO(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("external_urls") Map<String, String> externalUrls,
            @JsonProperty("images") List<CoverImageDetailsDTO> images,
            @JsonProperty("public") boolean isPublic,
            @JsonProperty("collaborative") boolean isCollaborative
    ) {
        this(
                id,
                name,
                description,
                extractFirstImage(images),
                extractFirstUrl(externalUrls),
                isPublic,
                isCollaborative
        );
    }

    private static String extractFirstUrl(Map<String, String> externalUrls) {
        if (externalUrls != null && !externalUrls.isEmpty()) {
            return externalUrls.values().iterator().next();
        }
        return null;
    }

    private static CoverImageDetailsDTO extractFirstImage(List<CoverImageDetailsDTO> images) {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }
}
