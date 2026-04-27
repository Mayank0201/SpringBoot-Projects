package com.example.cinetrackerbackend.gamification;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyXpTrackerRepository extends JpaRepository<DailyXpTracker, Long> {
    Optional<DailyXpTracker> findByUserIdAndActionTypeAndActionDate(Long userId, String actionType, LocalDate actionDate);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.count) FROM DailyXpTracker d WHERE d.userId = :userId AND d.actionType = :actionType AND d.actionDate >= :startDate")
    Integer sumCountByUserIdAndActionTypeAfter(Long userId, String actionType, LocalDate startDate);
}
