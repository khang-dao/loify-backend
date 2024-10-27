package cloud.loify.packages.common.dto;


import java.util.List;

public record TrackItemObjectDTO(String id, String name, String preview_url, AlbumDetailsDTO album, List<ArtistDetailsDTO> artists) implements TrackItem {}
