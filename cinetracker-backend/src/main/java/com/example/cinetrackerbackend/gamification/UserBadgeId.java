package com.example.cinetrackerbackend.gamification;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeId implements Serializable {
    private Long userId;
    private Long badgeId;
}
