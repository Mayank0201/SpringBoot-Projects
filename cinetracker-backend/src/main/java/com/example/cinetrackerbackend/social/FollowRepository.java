package com.example.cinetrackerbackend.social;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollower_IdAndFollowing_Id(Long followerId, Long followingId);
    
    List<Follow> findByFollower_Id(Long followerId);
    
    List<Follow> findByFollowing_Id(Long followingId);
    
    long countByFollower_Id(Long followerId);
    
    long countByFollowing_Id(Long followingId);
    
    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);
    
    void deleteByFollower_IdAndFollowing_Id(Long followerId, Long followingId);
}
