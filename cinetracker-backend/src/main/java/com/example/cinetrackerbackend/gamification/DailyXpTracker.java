package com.example.cinetrackerbackend.gamification;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "daily_xp_tracker", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "action_type", "action_date"}))
public class DailyXpTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "action_date", nullable = false)
    private LocalDate actionDate;

    @Column(nullable = false)
    private Integer count = 0;

    public DailyXpTracker(Long userId, String actionType, LocalDate actionDate) {
        this.userId = userId;
        this.actionType = actionType;
        this.actionDate = actionDate;
        this.count = 0;
    }
}
