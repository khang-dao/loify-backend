package cloud.loify.packages.track.dto;


import cloud.loify.packages.common.dto.AlbumDetailsDTO;
import cloud.loify.packages.common.dto.ArtistDetailsDTO;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(using = TrackDetailsDTODeserializer.class)
public record TrackDetailsDTO(String id, String name, AlbumDetailsDTO album,
                              List<ArtistDetailsDTO> artists, Boolean explicit,  String url) {
}
