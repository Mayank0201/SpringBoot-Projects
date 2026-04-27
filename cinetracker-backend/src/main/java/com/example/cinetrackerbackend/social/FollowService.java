package com.example.cinetrackerbackend.social;

import com.example.cinetrackerbackend.gamification.GamificationService;


import com.example.cinetrackerbackend.user.User;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new ApiException("You cannot follow yourself", HttpStatus.BAD_REQUEST);
        }

        if (followRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId)) {
            throw new ApiException("Already following this user", HttpStatus.CONFLICT);
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ApiException("Follower not found", HttpStatus.NOT_FOUND));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new ApiException("User to follow not found", HttpStatus.NOT_FOUND));

        Follow follow = new Follow(follower, following);
        followRepository.save(follow);
        
        // Award XP
        gamificationService.awardXpWithLimit(followerId, GamificationService.XP_FOLLOW_USER, "FOLLOW", "Followed user: " + following.getUsername());
    }



    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        if (!followRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId)) {
            throw new ApiException("Not following this user", HttpStatus.NOT_FOUND);
        }
        followRepository.deleteByFollower_IdAndFollowing_Id(followerId, followingId);
    }

    public List<Long> getFollowingIds(Long userId) {
        return followRepository.findByFollower_Id(userId).stream()
                .map(f -> f.getFollowing().getId())
                .collect(Collectors.toList());
    }

    public List<Long> getFollowerIds(Long userId) {
        return followRepository.findByFollowing_Id(userId).stream()
                .map(f -> f.getFollower().getId())
                .collect(Collectors.toList());
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId);
    }
    
    public long getFollowerCount(Long userId) {
        return followRepository.countByFollowing_Id(userId);
    }
    
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollower_Id(userId);
    }
}
