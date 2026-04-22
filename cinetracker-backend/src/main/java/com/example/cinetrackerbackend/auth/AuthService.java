package com.example.cinetrackerbackend.auth;

import com.example.cinetrackerbackend.user.User;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.exception.ApiException;
import com.example.cinetrackerbackend.email.EmailService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.cinetrackerbackend.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService{

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final EmailService emailService;

  public User register(String username, String email, String password){

    if (userRepository.existsByUsername(username)){
      throw new ApiException("Username already exists", HttpStatus.CONFLICT);
    }

    if(userRepository.existsByEmail(email)){
      throw new ApiException("Email address already in use", HttpStatus.CONFLICT);
    }

    User user = new User(username, email, passwordEncoder.encode(password));
    user.setIsEmailVerified(false);

    // Generate verification token
    String verificationToken = generateVerificationToken();
    String tokenHash = hashToken(verificationToken);
    user.setEmailVerificationToken(tokenHash);
    user.setEmailVerificationTokenExpiresAt(Instant.now().plusSeconds(24 * 60 * 60)); // 24 hours

    User savedUser = userRepository.save(user);

    // Send verification email
    try {
      emailService.sendVerificationEmail(email, username, verificationToken);
    } catch (Exception e) {
      log.error("Failed to send verification email for user: {}", username, e);
      // Continue even if email fails, but log it
    }

    return savedUser;
  }

  public AuthTokenResponse login(String username, String password){

    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new ApiException("Invalid username or password", HttpStatus.UNAUTHORIZED));

    if(!passwordEncoder.matches(password, user.getPassword())){
      throw new ApiException("Invalid username or password", HttpStatus.UNAUTHORIZED);
    }

    // Check if email is verified
    if (!user.getIsEmailVerified()) {
      throw new ApiException("Please verify your email before logging in", HttpStatus.FORBIDDEN);
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

  public VerifyEmailResponse verifyEmail(String verificationToken) {
    String normalizedToken = verificationToken == null ? "" : verificationToken.trim();
    String tokenHash = hashToken(normalizedToken);

    User user = userRepository.findByEmailVerificationToken(tokenHash)
        .filter(u -> u.getEmailVerificationTokenExpiresAt() != null && 
                    u.getEmailVerificationTokenExpiresAt().isAfter(Instant.now()))
        .orElseThrow(() -> new ApiException("Invalid or expired verification token", HttpStatus.UNAUTHORIZED));
    user.setIsEmailVerified(true);

    user.setEmailVerificationToken(null);
    user.setEmailVerificationTokenExpiresAt(null);
    User updatedUser = userRepository.save(user);

    log.info("Email verified for user: {}", user.getUsername());
    return VerifyEmailResponse.of(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail());
  }

  public void resendVerificationEmail(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

    if (user.getIsEmailVerified()) {
      throw new ApiException("Email is already verified", HttpStatus.BAD_REQUEST);
    }

    // Generate new verification token
    String verificationToken = generateVerificationToken();
    user.setEmailVerificationToken(hashToken(verificationToken));
    user.setEmailVerificationTokenExpiresAt(Instant.now().plusSeconds(24 * 60 * 60)); // 24 hours
    userRepository.save(user);

    // Send verification email
    try {
      emailService.sendResendVerificationEmail(email, user.getUsername(), verificationToken);
      log.info("Resent verification email to: {}", email);
    } catch (Exception e) {
      log.error("Failed to resend verification email to: {}", email, e);
      throw new ApiException("Failed to send verification email", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private String generateVerificationToken() {
    SecureRandom random = new SecureRandom();
    byte[] tokenBytes = new byte[32];
    random.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
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
