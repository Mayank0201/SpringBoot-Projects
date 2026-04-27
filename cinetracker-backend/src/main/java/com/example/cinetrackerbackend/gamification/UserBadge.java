package com.example.cinetrackerbackend.gamification;


import com.example.cinetrackerbackend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_badges")
public class UserBadge {
    
    @EmbeddedId
    private UserBadgeId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("badgeId")
    @JoinColumn(name = "badge_id")
    private Badge badge;


    @Column(name = "earned_at")
    private LocalDateTime earnedAt = LocalDateTime.now();

    public UserBadge(User user, Badge badge) {
        this.id = new UserBadgeId(user.getId(), badge.getId());
        this.user = user;
        this.badge = badge;
        this.earnedAt = LocalDateTime.now();
    }
}
