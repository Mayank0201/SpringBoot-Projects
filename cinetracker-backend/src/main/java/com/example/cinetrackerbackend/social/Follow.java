package com.example.cinetrackerbackend.social;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.cinetrackerbackend.user.User;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "follows", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }
}
