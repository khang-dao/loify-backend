package com.example.demo.dto;

public record SpotifyAuthTokenResponseDTO (String access_token, String token_type, String expires_in) {}