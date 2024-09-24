package cloud.loify.dto.response;

import cloud.loify.dto.track.TracksDTO;


public record PlaylistResponseDTO(String href, String id, String name, TracksDTO tracks) {}