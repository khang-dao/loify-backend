package cloud.loify.dto;


import cloud.loify.dto.common.CoverImageDTO;
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