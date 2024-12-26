package cloud.loify.packages.playlist.dto;

import cloud.loify.packages.common.dto.CoverImageDetailsDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;

public class PlaylistDetailsDTODeserializer extends JsonDeserializer<PlaylistDetailsDTO> {
    @Override
    public PlaylistDetailsDTO deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        ObjectNode node = mapper.readTree(parser);

        String id = node.get("id").asText();
        String description = node.get("description").asText();
        String name = node.get("name").asText();

        // Deserialize images into a list
        List<CoverImageDetailsDTO> images = mapper.convertValue(
                node.get("images"),
                mapper.getTypeFactory().constructCollectionType(List.class, CoverImageDetailsDTO.class)
        );

        // Extract the first image
        CoverImageDetailsDTO firstImage = (images != null && !images.isEmpty()) ? images.get(0) : null;

        return new PlaylistDetailsDTO(id, description, name, firstImage);
    }
}
