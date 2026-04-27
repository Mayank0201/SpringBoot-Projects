package com.example.cinetrackerbackend.social;

import com.example.cinetrackerbackend.common.ApiResponse;
import com.example.cinetrackerbackend.rating.MovieRating;
import com.example.cinetrackerbackend.rating.RatingService;
import com.example.cinetrackerbackend.user.User;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.exception.ApiException;
import com.example.cinetrackerbackend.gamification.Badge;
import com.example.cinetrackerbackend.gamification.UserBadge;
import com.example.cinetrackerbackend.gamification.UserBadgeRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class SocialController {
    private final FollowService followService;
    private final RatingService ratingService;
    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;


    @PostMapping("/follow/{userId}")
    public ResponseEntity<ApiResponse<Void>> followUser(@PathVariable Long userId) {
        Long currentUserId = getAuthenticatedUserId();
        followService.followUser(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("User followed", 200, null));
    }

    @DeleteMapping("/unfollow/{userId}")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(@PathVariable Long userId) {
        Long currentUserId = getAuthenticatedUserId();
        followService.unfollowUser(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("User unfollowed", 200, null));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProfile(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        org.springframework.data.domain.Page<MovieRating> reviews = ratingService.getUserReviews(userId, page, size);
        long followers = followService.getFollowerCount(userId);
        long following = followService.getFollowingCount(userId);

        List<Badge> badges = userBadgeRepository.findByUserIdDirect(userId)


                .stream().map(UserBadge::getBadge).toList();
        
        boolean isFollowing = false;

        try {
            Long currentUserId = getAuthenticatedUserId();
            isFollowing = followService.isFollowing(currentUserId, userId);
        } catch (Exception e) {
            // Unauthenticated user
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("xp", user.getXp());
        profile.put("level", user.getLevel());
        profile.put("reviews", reviews);

        profile.put("followerCount", followers);
        profile.put("followingCount", following);
        profile.put("isFollowing", isFollowing);
        profile.put("badges", badges);

        return ResponseEntity.ok(ApiResponse.success("Profile fetched", 200, profile));
    }

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        User user = (User) auth.getPrincipal();
        return user.getId();
    }
}
