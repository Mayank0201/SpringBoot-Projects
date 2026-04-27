package com.example.cinetrackerbackend.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data

// users table stores the core user account information
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

    @Column(nullable = false)
    private Long xp = 0L;

    @Column(nullable = false)
    private Integer level = 1;

    public User(String username, String email, String password) {
        this.username=username;
        this.email=email;
        this.password=password;
        this.isEmailVerified = false;
        this.xp = 0L;
        this.level = 1;
    }

}
