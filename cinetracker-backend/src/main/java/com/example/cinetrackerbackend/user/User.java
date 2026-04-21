package com.example.cinetrackerbackend.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.time.Instant;

@Entity
@RequiredArgsConstructor
@Data
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @Column(nullable=false,unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable=false,unique=false)
    private String password;

    @Column(name = "refresh_token_hash")
    private String refreshTokenHash;

    @Column(name = "refresh_token_expires_at")
    private Instant refreshTokenExpiresAt;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expires_at")
    private Instant emailVerificationTokenExpiresAt;

    public User(String username, String email, String password) {
        this.username=username;
        this.email=email;
        this.password=password;
        this.isEmailVerified = false;
    }
}
