package com.example.cinetrackerbackend.auth;

public record RefreshTokenResponse(
    String token,
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn
) {
    public static RefreshTokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new RefreshTokenResponse(accessToken, accessToken, refreshToken, "Bearer", expiresIn);
    }
}
