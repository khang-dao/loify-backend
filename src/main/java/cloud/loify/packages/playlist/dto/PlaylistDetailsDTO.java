package cloud.loify.packages.playlist.dto;

import cloud.loify.packages.common.dto.CoverImageDetailsDTO;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@JsonDeserialize(using = PlaylistDetailsDTODeserializer.class)
public record PlaylistDetailsDTO(String id, String description, String name, CoverImageDetailsDTO image) {}
