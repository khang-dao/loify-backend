package cloud.loify.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatePlaylistResponseDTO(String id, String name, String description, @JsonProperty("public") boolean isPublic, boolean collaborative) {}