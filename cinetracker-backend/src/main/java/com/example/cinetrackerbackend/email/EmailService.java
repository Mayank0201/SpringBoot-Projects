package com.example.cinetrackerbackend.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    public void sendVerificationEmail(String userEmail, String username, String verificationToken) {
        // Log token for dev testing
        log.info("Generated verification token for {}: {}", userEmail, verificationToken);
        
        // Skip email sending if mail credentials are not configured
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("Mail credentials not configured. Skipping email to: {} (Token: {})", userEmail, verificationToken.substring(0, Math.min(10, verificationToken.length())));
            return;
        }

        try {
            String verificationLink = frontendBaseUrl + "/verify-email?token=" + verificationToken;
            String subject = "Verify Your Email - CineTracker";
            String body = buildVerificationEmailBody(username, verificationLink);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            log.info("Verification email sent to: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", userEmail, e);
            log.warn("Email sending failed, but user registration will continue");
        }
    }

    public void sendResendVerificationEmail(String userEmail, String username, String verificationToken) {
        sendVerificationEmail(userEmail, username, verificationToken);
    }

    private String buildVerificationEmailBody(String username, String verificationLink) {
        return "Hello " + username + ",\n\n" +
                "Welcome to CineTracker! Please verify your email address by clicking the link below:\n\n" +
                verificationLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create this account, you can safely ignore this email.\n\n" +
                "Best regards,\n" +
                "CineTracker Team";
    }
}
