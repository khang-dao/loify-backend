package cloud.loify.dto;

public record CreatePlaylistRequestDTO(String name, String description, boolean isPublic, boolean collaborative) {

    @Override
    public String toString() {
        return "{" +
                "\"name\": \"" + name + "\"," +
                "\"description\": \"" + description + "\"," +
                "\"public\": " + isPublic + "," +
                "\"collaborative\": " + collaborative +
                "}";
    }
}
