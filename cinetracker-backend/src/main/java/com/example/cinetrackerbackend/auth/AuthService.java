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
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Map;

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
    log.info("Starting registration process for username: {}. Note: User data will NOT be persisted to Supabase until email verification is complete.", username);

    if (com.example.cinetrackerbackend.common.ContentModerator.containsAnyProfanity(username)) {
      throw new ApiException("Restricted language detected. Please refrain from using offensive words.", HttpStatus.BAD_REQUEST);
    }

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
    // They will be saved only upon successful email verification in verifyEmail().
    User transientUser = new User(username, email, encodedPassword);
    transientUser.setIsEmailVerified(false);
    
    log.info("Registration request processed for {}. Verification email sent. User remains transient.", username);
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
    String newRefreshToken = jwtService.generateRefreshToken(user);

    // Update user with new refresh token hash and expiration (Refresh Token Rotation)
    user.setRefreshTokenHash(hashToken(newRefreshToken));
    user.setRefreshTokenExpiresAt(jwtService.extractExpiration(newRefreshToken).toInstant());
    userRepository.save(user);

    long expiresInSeconds = jwtService.extractExpiration(newAccessToken).toInstant().getEpochSecond()
        - Instant.now().getEpochSecond();

    return RefreshTokenResponse.of(newAccessToken, newRefreshToken, Math.max(expiresInSeconds, 0));
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

    // Extract details from token
    Claims claims = jwtService.extractEmailVerificationClaims(normalizedToken);
    String username = claims.getSubject();
    String email = claims.get("email", String.class);
    String encodedPassword = claims.get("password", String.class);

    // Final check to ensure username/email hasn't been taken by someone else who verified faster
    if (userRepository.existsByUsername(username)) {
      throw new ApiException("This username has already been taken and verified.", HttpStatus.CONFLICT);
    }
    if (userRepository.existsByEmail(email)) {
      throw new ApiException("This email has already been verified for another account.", HttpStatus.CONFLICT);
    }

    // Create and save the user only now
    User user = new User(username, email, encodedPassword);
    user.setIsEmailVerified(true);
    
    User savedUser = userRepository.save(user);
    log.info("Email verification successful. User '{}' has been persisted to the database.", username);

    return VerifyEmailResponse.of(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
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

  public void forgotPassword(String email) {
    String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
    if (normalizedEmail.isEmpty()) {
      throw new ApiException("Email is required", HttpStatus.BAD_REQUEST);
    }

    User user = userRepository.findByEmail(normalizedEmail)
        .orElseThrow(() -> new ApiException("No account found with this email", HttpStatus.NOT_FOUND));

    if (!user.getIsEmailVerified()) {
      throw new ApiException("Email is not verified. Please verify your email first.", HttpStatus.FORBIDDEN);
    }

    String resetToken = UUID.randomUUID().toString().replace("-", "");
    try {
      stringRedisTemplate.opsForValue().set("password_reset:" + resetToken, user.getEmail(), Duration.ofMinutes(15));
    } catch (Exception e) {
      log.error("Failed to connect to Redis while storing reset token", e);
      throw new ApiException("Service is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    CompletableFuture.runAsync(() -> {
      try {
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetToken);
      } catch (Exception e) {
        log.error("Failed to send password reset email for user: {}", user.getUsername(), e);
      }
    });
  }

  public void resetPassword(String token, String newPassword) {
    String normalizedToken = token == null ? "" : token.trim();
    if (normalizedToken.isEmpty()) {
      throw new ApiException("Reset token is required", HttpStatus.BAD_REQUEST);
    }

    String email;
    try {
      email = stringRedisTemplate.opsForValue().get("password_reset:" + normalizedToken);
    } catch (Exception e) {
      log.error("Failed to connect to Redis while fetching reset token", e);
      throw new ApiException("Service is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    if (email == null) {
      throw new ApiException("Invalid or expired reset token", HttpStatus.UNAUTHORIZED);
    }

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    try {
      stringRedisTemplate.delete("password_reset:" + normalizedToken);
    } catch (Exception e) {
      log.warn("Failed to delete used reset token from Redis", e);
    }

    log.info("Password successfully reset for user '{}'", user.getUsername());
  }

  public AuthTokenResponse googleLogin(String idToken) {
    if (idToken == null || idToken.trim().isEmpty()) {
      throw new ApiException("Google ID Token is required", HttpStatus.BAD_REQUEST);
    }

    String googleVerifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken.trim();
    RestTemplate restTemplate = new RestTemplate();
    try {
      ResponseEntity<Map> response = restTemplate.getForEntity(googleVerifyUrl, Map.class);
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        Map<String, Object> body = response.getBody();
        String email = (String) body.get("email");
        if (email == null || email.trim().isEmpty()) {
          throw new ApiException("Google token did not contain an email", HttpStatus.BAD_REQUEST);
        }
        email = email.trim().toLowerCase();

        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        User user;
        if (existingUserOpt.isPresent()) {
          user = existingUserOpt.get();
          if (!user.getIsEmailVerified()) {
            user.setIsEmailVerified(true);
            userRepository.save(user);
          }
        } else {
          // Register a new user
          String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
          if (baseUsername.isEmpty()) {
            baseUsername = "user";
          }
          String username = baseUsername;
          int counter = 1;
          while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
          }

          String randomPassword = UUID.randomUUID().toString();
          user = new User(username, email, passwordEncoder.encode(randomPassword));
          user.setIsEmailVerified(true);
          user = userRepository.save(user);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshTokenHash(hashToken(refreshToken));
        user.setRefreshTokenExpiresAt(jwtService.extractExpiration(refreshToken).toInstant());
        userRepository.save(user);

        long expiresInSeconds = jwtService.extractExpiration(accessToken).toInstant().getEpochSecond()
            - Instant.now().getEpochSecond();

        return AuthTokenResponse.of(accessToken, refreshToken, Math.max(expiresInSeconds, 0));
      } else {
        throw new ApiException("Invalid Google ID Token", HttpStatus.UNAUTHORIZED);
      }
    } catch (ApiException e) {
      throw e;
    } catch (Exception e) {
      log.error("Google authentication failed", e);
      throw new ApiException("Google authentication failed. Please try again.", HttpStatus.UNAUTHORIZED);
    }
  }
}
