package cloud.loify.packages.common.dto;


import java.util.List;

public record AlbumDetailsDTO(String id, String name, List<CoverImageDTO> images) {}

