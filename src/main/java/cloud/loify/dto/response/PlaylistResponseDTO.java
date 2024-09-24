package cloud.loify.dto.response;

import cloud.loify.dto.common.CoverImageDTO;
import cloud.loify.dto.track.TracksDTO;

import java.util.List;


public record PlaylistResponseDTO(String href, String id, String name, TracksDTO tracks, List<CoverImageDTO> images) {}