package cloud.loify.packages.me.dto;


import cloud.loify.packages.playlist.dto.PlaylistDetailsDTO;

import java.util.List;

public record GetUserPlaylistsResponseDTO(List<PlaylistDetailsDTO> items) {
}
