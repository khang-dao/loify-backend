package cloud.loify.packages.playlist.dto;

import cloud.loify.packages.common.dto.CoverImageDTO;
import cloud.loify.packages.track.dto.GetTracksResponseDTO;

import java.util.List;


public record GetPlaylistResponseDTO(String href, String id, String name, GetTracksResponseDTO tracks, List<CoverImageDTO> images) {}