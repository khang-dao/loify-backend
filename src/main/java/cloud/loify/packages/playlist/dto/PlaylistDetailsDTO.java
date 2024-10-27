package cloud.loify.packages.playlist.dto;

import cloud.loify.packages.common.dto.CoverImageDetailsDTO;

import java.util.List;


public record PlaylistDetailsDTO(String id, String description, String name, List<CoverImageDetailsDTO> images) {}
