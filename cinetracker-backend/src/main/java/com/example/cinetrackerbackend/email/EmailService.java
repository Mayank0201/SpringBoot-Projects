package com.example.cinetrackerbackend.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${email.from-address:noreply@cinefolio.app}")
    private String fromEmail;

    @Value("${email.from-name:CineFolio}")
    private String fromName;

    @Value("${app.backend.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    public void sendVerificationEmail(String userEmail, String username, String verificationToken) {
        // Log token for dev testing
        log.info("Generated verification token for {}: {}", userEmail, verificationToken);

        // Skip email sending if Brevo API key is not configured
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            log.warn("Brevo API key not configured. Skipping email to: {} (Token: {})", userEmail, verificationToken.substring(0, Math.min(10, verificationToken.length())));
            return;
        }

        try {
            String encodedToken = URLEncoder.encode(verificationToken, StandardCharsets.UTF_8);
            String normalizedBaseUrl = backendBaseUrl == null ? "http://localhost:8080" : backendBaseUrl.replaceAll("/+$", "");
            String verificationLink = normalizedBaseUrl + "/auth/verify-email?token=" + encodedToken;
            String subject = "Verify Your Email - CineFolio";
            String body = buildVerificationEmailBody(username, verificationLink);

            sendEmailViaBrevo(userEmail, subject, body);
            log.info("Verification email sent to: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", userEmail, e);
            log.warn("Email sending failed, but user registration will continue");
        }
    }

    public void sendResendVerificationEmail(String userEmail, String username, String verificationToken) {
        sendVerificationEmail(userEmail, username, verificationToken);
    }

    private void sendEmailViaBrevo(String toEmail, String subject, String textContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        Map<String, Object> sender = new HashMap<>();
        sender.put("name", fromName);
        sender.put("email", fromEmail);

        Map<String, String> toRecipient = new HashMap<>();
        toRecipient.put("email", toEmail);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("sender", sender);
        requestBody.put("to", List.of(toRecipient));
        requestBody.put("subject", subject);
        requestBody.put("textContent", textContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BREVO_API_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Brevo API returned status: " + response.getStatusCode() + " body: " + response.getBody());
        }

        log.debug("Brevo API response: {}", response.getBody());
    }

    private String buildVerificationEmailBody(String username, String verificationLink) {
        return "Hello " + username + ",\n\n" +
                "Welcome to CineFolio! Please verify your email address by clicking the link below:\n\n" +
                verificationLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create this account, you can safely ignore this email.\n\n" +
                "Best regards,\n" +
                "CineFolio Team";
    }
}
