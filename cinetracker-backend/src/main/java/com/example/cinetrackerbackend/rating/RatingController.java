package com.example.cinetrackerbackend.rating;

import com.example.cinetrackerbackend.common.ApiResponse;
import com.example.cinetrackerbackend.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PutMapping("/{movieId}/rating")
    public ResponseEntity<ApiResponse<MovieRating>> setRating(
            @PathVariable Long movieId,
            @Valid @RequestBody RatingRequest request) {
        Long userId = getAuthenticatedUserId();
        MovieRating rating = ratingService.setRating(movieId, userId, request.getRating());
        return ResponseEntity.ok(ApiResponse.success("Rating saved", 200, rating));
    }

    @DeleteMapping("/{movieId}/rating")
    public ResponseEntity<ApiResponse<Void>> deleteRating(@PathVariable Long movieId) {
        Long userId = getAuthenticatedUserId();
        ratingService.deleteRating(movieId, userId);
        return ResponseEntity.ok(ApiResponse.success("Rating deleted", 200, null));
    }

    @GetMapping("/{movieId}/rating-summary")
    public ResponseEntity<ApiResponse<RatingSummaryDTO>> getRatingSummary(
            @PathVariable Long movieId,
            Authentication authentication) {

        RatingSummaryDTO summary;
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            Long userId = user.getId();
            summary = ratingService.getRatingSummary(movieId, userId);
        } else {
            summary = ratingService.getRatingSummary(movieId);
        }
        return ResponseEntity.ok(ApiResponse.success("Rating summary fetched", 200, summary));
    }

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        return user.getId();
    }
}
