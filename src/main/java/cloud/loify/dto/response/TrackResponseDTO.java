package cloud.loify.dto.response;

import cloud.loify.dto.track.TrackItemDTO;

import java.util.List;

public record TrackResponseDTO(List<TrackItemDTO> items) {}