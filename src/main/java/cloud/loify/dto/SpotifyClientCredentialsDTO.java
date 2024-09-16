package cloud.loify.dto;

// Convert to Builder Pattern later?
public record SpotifyClientCredentialsDTO(String grant_type, String client_id, String client_secret, String redirectUri, String authCode) {
    @Override
    public String toString() {
        return "grant_type=" + grant_type +
                "&client_id=" + client_id +
                "&client_secret=" + client_secret +
                "&redirect_uri=" + redirectUri  +
                "&code=" + authCode;
    }
}
