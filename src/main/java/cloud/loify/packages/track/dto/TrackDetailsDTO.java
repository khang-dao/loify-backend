package cloud.loify.packages.track.dto;


import cloud.loify.packages.common.dto.AlbumDetailsDTO;
import cloud.loify.packages.common.dto.ArtistDetailsDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TrackDetailsDTO(String id, String name, @JsonProperty("preview_url") String url, AlbumDetailsDTO album, List<ArtistDetailsDTO> artists) {}
