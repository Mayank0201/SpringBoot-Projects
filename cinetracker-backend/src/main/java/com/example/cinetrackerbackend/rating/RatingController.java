package com.example.cinetrackerbackend.rating;

import com.example.cinetrackerbackend.user.User;
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
    public ResponseEntity<MovieRating> setRating(
            @PathVariable Long movieId,
            @RequestBody RatingRequest request) {

        if (request.getRating() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Long userId = getAuthenticatedUserId();
            MovieRating rating = ratingService.setRating(movieId, userId, request.getRating());
            return ResponseEntity.ok(rating);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{movieId}/rating")
    public ResponseEntity<Void> deleteRating(@PathVariable Long movieId) {

        try {
            Long userId = getAuthenticatedUserId();
            ratingService.deleteRating(movieId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{movieId}/rating-summary")
    public ResponseEntity<RatingSummaryDTO> getRatingSummary(
            @PathVariable Long movieId,
            Authentication authentication) {

        try {
            RatingSummaryDTO summary;
            if (authentication != null && authentication.getPrincipal() instanceof User user) {
                Long userId = user.getId();
                summary = ratingService.getRatingSummary(movieId, userId);
            } else {
                summary = ratingService.getRatingSummary(movieId);
            }
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        return user.getId();
    }
}
