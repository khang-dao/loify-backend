package cloud.loify.dto;

public record SpotifyAuthTokenResponseDTO (String access_token, String token_type, String expires_in) {}