package cloud.loify.dto;


public record CreatePlaylistResponseDTO(String id, String name, String description, boolean isPublic, boolean collaborative) {}