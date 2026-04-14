package com.example.cinetrackerbackend.auth;

public record AuthTokenResponse(
	String token,
	String accessToken,
	String refreshToken,
	String tokenType,
	long expiresIn
) {
	public static AuthTokenResponse of(String accessToken, String refreshToken, long expiresIn) {
		return new AuthTokenResponse(accessToken, accessToken, refreshToken, "Bearer", expiresIn);
	}
}
