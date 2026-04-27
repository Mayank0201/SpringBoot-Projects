package com.example.cinetrackerbackend.gamification;


import com.example.cinetrackerbackend.user.User;
import com.example.cinetrackerbackend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {
    private final UserRepository userRepository;
    private final DailyXpTrackerRepository xpTrackerRepository;

    // XP constants
    public static final int XP_ADD_WATCHLIST = 10;
    public static final int XP_COMPLETE_MOVIE = 50;
    public static final int XP_RATE_MOVIE = 30;
    public static final int XP_FOLLOW_USER = 15;

    // Daily Limits
    private static final int LIMIT_FOLLOW = 1;
    private static final int LIMIT_ADD_WATCHLIST = 3;
    private static final int LIMIT_RATE_MOVIE = 2;
    private static final int LIMIT_COMPLETE_MOVIE = 2;

    @Transactional
    public void awardXp(Long userId, int amount, String reason) {
        // Direct XP award (no limit, used for challenges)
        applyXp(userId, amount, reason);
    }

    @Transactional
    public void awardXpWithLimit(Long userId, int amount, String action, String reason) {
        LocalDate today = LocalDate.now();
        DailyXpTracker tracker = xpTrackerRepository.findByUserIdAndActionTypeAndActionDate(userId, action, today)
                .orElse(new DailyXpTracker(userId, action, today));
        
        int limit = getLimitForAction(action);
        
        if (tracker.getCount() < limit) {
            applyXp(userId, amount, reason);
            tracker.setCount(tracker.getCount() + 1);
            xpTrackerRepository.save(tracker);
        } else {
            log.info("Daily XP limit reached for user {} on action {}", userId, action);
        }
    }

    private void applyXp(Long userId, int amount, String reason) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setXp(user.getXp() + amount);
            
            // Dynamic Leveling Formula: 50 * L * (L-1)
            // L2: 100, L3: 300, L4: 600, L5: 1000...
            int currentLevel = user.getLevel();
            int nextLevelXp = 50 * (currentLevel + 1) * currentLevel;
            
            while (user.getXp() >= nextLevelXp) {
                currentLevel++;
                nextLevelXp = 50 * (currentLevel + 1) * currentLevel;
            }
            
            if (currentLevel > user.getLevel()) {
                user.setLevel(currentLevel);
            }
            userRepository.save(user);

            log.info("Awarded {} XP to user {} for: {}", amount, user.getUsername(), reason);
        });
    }

    public int getDailyActionCount(Long userId, String action) {
        return xpTrackerRepository.findByUserIdAndActionTypeAndActionDate(userId, action, LocalDate.now())
                .map(DailyXpTracker::getCount)
                .orElse(0);
    }

    public int getWeeklyActionCount(Long userId, String action) {
        // Assume "weekly" means last 7 days including today
        LocalDate startDate = LocalDate.now().minusDays(7);
        Integer count = xpTrackerRepository.sumCountByUserIdAndActionTypeAfter(userId, action, startDate);
        return count != null ? count : 0;
    }

    private int getLimitForAction(String action) {

        return switch (action) {
            case "FOLLOW" -> LIMIT_FOLLOW;
            case "ADD_WATCHLIST" -> LIMIT_ADD_WATCHLIST;
            case "RATE" -> LIMIT_RATE_MOVIE;
            case "COMPLETE" -> LIMIT_COMPLETE_MOVIE;
            default -> 999;
        };
    }
}


