package com.example.cinetrackerbackend.auth;

import com.example.cinetrackerbackend.user.User;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.exception.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.cinetrackerbackend.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService{

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public User register(String username,String email,String password){

    if (userRepository.existsByUsername(username)){
      throw new ApiException("Username already exists", HttpStatus.CONFLICT);
    }

    if(userRepository.existsByEmail(email)){
      throw new ApiException("Email address already in use", HttpStatus.CONFLICT);
    }

    User user = new User(username, email, passwordEncoder.encode(password));
    User savedUser = userRepository.save(user);
    return savedUser;
  }

  public AuthTokenResponse login(String username,String password){

    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new ApiException("Invalid username or password", HttpStatus.UNAUTHORIZED));

    if(!passwordEncoder.matches(password, user.getPassword())){
      throw new ApiException("Invalid username or password", HttpStatus.UNAUTHORIZED);
    }

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    user.setRefreshTokenHash(hashToken(refreshToken));
    user.setRefreshTokenExpiresAt(jwtService.extractExpiration(refreshToken).toInstant());
    userRepository.save(user);

    long expiresInSeconds = jwtService.extractExpiration(accessToken).toInstant().getEpochSecond()
        - Instant.now().getEpochSecond();

    return AuthTokenResponse.of(accessToken, refreshToken, Math.max(expiresInSeconds, 0));
  }

  public RefreshTokenResponse refreshAccessToken(String refreshToken) {

    String username;
    try {
      username = jwtService.extractUsername(refreshToken);
    } catch (Exception ex) {
      throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
    }

    String refreshTokenHash = hashToken(refreshToken);

    User user = userRepository.findByRefreshTokenHash(refreshTokenHash)
        .orElseThrow(() -> new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

    if (!user.getUsername().equals(username)
        || !jwtService.isRefreshTokenValid(refreshToken, username)
        || user.getRefreshTokenExpiresAt() == null
        || user.getRefreshTokenExpiresAt().isBefore(Instant.now())) {
      throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
    }

    String newAccessToken = jwtService.generateAccessToken(user);
    long expiresInSeconds = jwtService.extractExpiration(newAccessToken).toInstant().getEpochSecond()
        - Instant.now().getEpochSecond();

    return RefreshTokenResponse.of(newAccessToken, Math.max(expiresInSeconds, 0));
  }

  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hashBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm is not available", e);
    }
  }

}
