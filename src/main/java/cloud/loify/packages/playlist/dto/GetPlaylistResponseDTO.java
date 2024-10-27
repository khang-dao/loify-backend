package cloud.loify.packages.playlist.dto;

import cloud.loify.packages.common.dto.CoverImageDetailsDTO;
import cloud.loify.packages.track.dto.GetTracksFromPlaylistResponseDTO;

import java.util.List;


public record GetPlaylistResponseDTO(String href, String id, String name, GetTracksFromPlaylistResponseDTO tracks, List<CoverImageDetailsDTO> images) {}