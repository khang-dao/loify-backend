package cloud.loify.packages.playlist.dto;


import cloud.loify.packages.common.dto.CoverImageDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreatePlaylistResponseDTO(
        String href,
        String id,
        String name,
        @JsonProperty("external_urls") Object externalUrls,
        String description,
        @JsonProperty("public") boolean isPublic,
        boolean collaborative,
        List<CoverImageDTO> images) {}