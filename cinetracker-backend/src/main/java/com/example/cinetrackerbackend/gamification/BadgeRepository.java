package com.example.cinetrackerbackend.gamification;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    java.util.Optional<Badge> findByName(String name);
}

