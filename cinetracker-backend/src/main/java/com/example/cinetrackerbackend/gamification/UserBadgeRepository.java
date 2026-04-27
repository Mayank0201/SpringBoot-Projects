package com.example.cinetrackerbackend.gamification;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadgeId> {
    @org.springframework.data.jpa.repository.Query("SELECT ub FROM UserBadge ub WHERE ub.id.userId = :userId")
    List<UserBadge> findByUserIdDirect(Long userId);
}
