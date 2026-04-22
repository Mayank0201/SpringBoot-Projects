package com.example.cinetrackerbackend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshTokenHash(String refreshTokenHash);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByEmailVerificationToken(String emailVerificationToken);

    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerificationToken = null, u.emailVerificationTokenExpiresAt = null WHERE u.emailVerificationTokenExpiresAt < ?1")
    int deleteByEmailVerificationTokenExpiresAtBefore(Instant expiresAt);
}
