package cloud.loify.packages.track.dto;


import cloud.loify.packages.common.dto.AlbumDetailsDTO;
import cloud.loify.packages.common.dto.ArtistDetailsDTO;

import java.util.List;

public record TrackDetailsDTO(String id, String name, String preview_url, AlbumDetailsDTO album, List<ArtistDetailsDTO> artists) {}
