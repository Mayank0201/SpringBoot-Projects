package com.example.cinetrackerbackend.auth;

public record RefreshTokenResponse(
    String token,
    String accessToken,
    String tokenType,
    long expiresIn
) {
    public static RefreshTokenResponse of(String accessToken, long expiresIn) {
        return new RefreshTokenResponse(accessToken, accessToken, "Bearer", expiresIn);
    }
}
