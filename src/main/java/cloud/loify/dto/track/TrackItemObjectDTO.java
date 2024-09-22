package cloud.loify.dto.track;


import cloud.loify.dto.album.AlbumDTO;
import cloud.loify.dto.artist.ArtistDTO;

import java.util.List;

public record TrackItemObjectDTO(String id, String name, String preview_url, AlbumDTO album, List<ArtistDTO> artists) implements TrackItem {}
