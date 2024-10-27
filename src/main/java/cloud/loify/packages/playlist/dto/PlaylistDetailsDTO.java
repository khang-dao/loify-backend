package cloud.loify.packages.playlist.dto;

import cloud.loify.packages.common.dto.CoverImageDTO;

import java.util.List;


public record PlaylistDetailsDTO(String id, String description, String name, List<CoverImageDTO> images) {}
