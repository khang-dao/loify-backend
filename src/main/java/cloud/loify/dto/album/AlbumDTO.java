package cloud.loify.dto.album;


import cloud.loify.dto.common.CoverImageDTO;

import java.util.List;

public record AlbumDTO(String id, String name, List<CoverImageDTO> images) {}

