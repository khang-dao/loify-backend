package cloud.loify.dto.playlist;

import cloud.loify.dto.common.CoverImageDTO;
import cloud.loify.dto.track.TracksDTO;

import java.util.List;

public record PlaylistItemDTO(String id, String name, List<CoverImageDTO> images, List<TracksDTO> tracks) {}