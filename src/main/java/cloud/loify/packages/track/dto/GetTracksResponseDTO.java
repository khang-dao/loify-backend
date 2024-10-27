package cloud.loify.packages.track.dto;

import cloud.loify.packages.common.dto.TrackItem;

import java.util.List;


public record GetTracksResponseDTO(List<TrackItem> items) {}
