package cloud.loify.packages.track.dto;

import cloud.loify.packages.common.dto.AlbumDetailsDTO;
import cloud.loify.packages.common.dto.ArtistDetailsDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;

public class TrackDetailsDTODeserializer extends JsonDeserializer<TrackDetailsDTO> {
    @Override
    public TrackDetailsDTO deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        ObjectNode node = mapper.readTree(parser);

        // Extract basic fields
        String id = node.get("id").asText();
        String name = node.get("name").asText();
        Boolean explicit = node.get("explicit").asBoolean();
        String url = node.get("external_urls").get("spotify").asText();

        // Deserialize the album details
        AlbumDetailsDTO album = mapper.convertValue(
                node.get("album"),
                AlbumDetailsDTO.class
        );

        // Deserialize the artists into a list
        List<ArtistDetailsDTO> artists = mapper.convertValue(
                node.get("artists"),
                mapper.getTypeFactory().constructCollectionType(List.class, ArtistDetailsDTO.class)
        );

        // Create and return the TrackDetailsDTO instance
        return new TrackDetailsDTO(id, name, album, artists, explicit, url);
    }
}
