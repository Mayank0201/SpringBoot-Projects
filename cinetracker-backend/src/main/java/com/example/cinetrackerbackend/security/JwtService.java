package com.example.cinetrackerbackend.security;

import com.example.cinetrackerbackend.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;
import com.example.cinetrackerbackend.config.JwtConfig;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.Base64;
import java.security.Key;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtConfig jwtConfig;

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtConfig.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        return generateToken(user, jwtConfig.getExpiration(), ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, jwtConfig.getRefreshExpiration(), REFRESH_TOKEN_TYPE);
    }

    // Backward-compatible alias for existing callers.
    public String generateToken(User user) {
        return generateAccessToken(user);
    }

    private String generateToken(User user, long expirationInMillis, String tokenType) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationInMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        if (userId instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (userId instanceof Long longValue) {
            return longValue;
        }
        return null;
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public String extractTokenType(String token) {
        Object tokenType = extractAllClaims(token).get(TOKEN_TYPE_CLAIM);
        return tokenType != null ? tokenType.toString() : null;
    }

    public boolean isAccessTokenValid(String token, String username) {
        return isTokenValid(token, username, ACCESS_TOKEN_TYPE);
    }

    public boolean isRefreshTokenValid(String token, String username) {
        return isTokenValid(token, username, REFRESH_TOKEN_TYPE);
    }

    public boolean isTokenValid(String token, String username) {
        return isAccessTokenValid(token, username);
    }

    public boolean isTokenValid(String token, String username, String expectedTokenType) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);

            return claims.getSubject().equals(username)
                    && expectedTokenType.equals(tokenType)
                    && expiration != null
                    && expiration.after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }
}