package cloud.loify.dto.response;


import cloud.loify.dto.playlist.PlaylistItemDTO;

import java.util.List;

public record UserPlaylistsResponseDTO(String href, String id, List<PlaylistItemDTO> items) {}