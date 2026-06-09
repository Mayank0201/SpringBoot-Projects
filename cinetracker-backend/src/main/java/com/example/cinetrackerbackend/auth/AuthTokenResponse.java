package com.example.cinetrackerbackend.auth;

public record AuthTokenResponse(
	String token,
	String accessToken,
	String refreshToken,
	String tokenType,
	long expiresIn,
	Boolean isNewUser
) {
	public static AuthTokenResponse of(String accessToken, String refreshToken, long expiresIn) {
		return new AuthTokenResponse(accessToken, accessToken, refreshToken, "Bearer", expiresIn, false);
	}

	public static AuthTokenResponse of(String accessToken, String refreshToken, long expiresIn, boolean isNewUser) {
		return new AuthTokenResponse(accessToken, accessToken, refreshToken, "Bearer", expiresIn, isNewUser);
	}
}
