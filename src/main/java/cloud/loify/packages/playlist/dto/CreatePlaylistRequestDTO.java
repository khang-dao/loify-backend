package cloud.loify.packages.playlist.dto;

public record CreatePlaylistRequestDTO(String name, String description, boolean isPublic, boolean collaborative) {}
