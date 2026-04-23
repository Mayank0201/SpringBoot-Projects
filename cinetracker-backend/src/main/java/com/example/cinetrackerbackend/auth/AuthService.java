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
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService{

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final EmailService emailService;
  private final StringRedisTemplate stringRedisTemplate;

  public User register(String username, String email, String password){

    if (userRepository.existsByUsername(username)){
      throw new ApiException("Username already exists", HttpStatus.CONFLICT);
    }

    if(userRepository.existsByEmail(email)){
      throw new ApiException("Email address already in use", HttpStatus.CONFLICT);
    }

    String encodedPassword = passwordEncoder.encode(password);

    // Generate verification token securely containing the user details
    String verificationToken = jwtService.generateEmailVerificationToken(username, email, encodedPassword);

    String shortToken = UUID.randomUUID().toString().replace("-", "");
    boolean useShortToken = false;
    try {
      stringRedisTemplate.opsForValue().set("email_verify:" + shortToken, verificationToken, Duration.ofHours(24));
      useShortToken = true;
    } catch (Exception e) {
      log.warn("Redis is unavailable, falling back to long JWT token for email verification", e);
    }
    
    String finalToken = useShortToken ? shortToken : verificationToken;

    // Send verification email asynchronously so the API responds instantly
    CompletableFuture.runAsync(() -> {
      try {
        emailService.sendVerificationEmail(email, username, finalToken);
      } catch (Exception e) {
        log.error("Failed to send verification email for user: {}", username, e);
      }
    });

    // We do NOT save the user to the database yet. 
    // They will be saved only upon successful email verification.
    User transientUser = new User(username, email, encodedPassword);
    transientUser.setIsEmailVerified(false);
    return transientUser;
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
    
    if (normalizedToken.length() < 100) {
      try {
        String longToken = stringRedisTemplate.opsForValue().get("email_verify:" + normalizedToken);
        if (longToken != null) {
          normalizedToken = longToken;
          stringRedisTemplate.delete("email_verify:" + verificationToken.trim());
        } else {
          throw new ApiException("Invalid or expired verification token", HttpStatus.UNAUTHORIZED);
        }
      } catch (ApiException e) {
        throw e;
      } catch (Exception e) {
        log.error("Failed to connect to Redis while verifying email", e);
        throw new ApiException("Verification service is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
      }
    }

    Claims claims;
    try {
      claims = jwtService.extractEmailVerificationClaims(normalizedToken);
    } catch (Exception e) {
      throw new ApiException("Invalid or expired verification token", HttpStatus.UNAUTHORIZED);
    }

    String username = claims.getSubject();
    String email = claims.get("email", String.class);
    String encodedPassword = claims.get("password", String.class);

    // Check again to avoid race conditions
    if (userRepository.existsByUsername(username)) {
      throw new ApiException("Username is already taken by a verified account.", HttpStatus.CONFLICT);
    }
    if (userRepository.existsByEmail(email)) {
      throw new ApiException("Email is already taken by a verified account.", HttpStatus.CONFLICT);
    }

    // Now save to database
    User user = new User(username, email, encodedPassword);
    user.setIsEmailVerified(true);
    
    User updatedUser = userRepository.save(user);

    log.info("Email verified and user saved to database: {}", user.getUsername());
    return VerifyEmailResponse.of(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail());
  }

  public void resendVerificationEmail(String email) {
    // Since we no longer save unverified users to the database, we don't have their details to resend.
    // Instead, if the email exists, they are already verified. 
    // If it doesn't, they need to register again.
    
    if (userRepository.existsByEmail(email)) {
      throw new ApiException("This email is already verified. You can log in.", HttpStatus.BAD_REQUEST);
    }

    throw new ApiException("User not found or unverified. Since we do not store unverified users for privacy, please register again to receive a new verification link.", HttpStatus.NOT_FOUND);
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
