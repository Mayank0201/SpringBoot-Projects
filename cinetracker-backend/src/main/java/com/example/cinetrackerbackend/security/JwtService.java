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

    private final JwtConfig jwtConfig;

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtConfig.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
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

    public boolean isTokenValid(String token, String username) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            return claims.getSubject().equals(username)
                    && expiration != null
                    && expiration.after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }
}