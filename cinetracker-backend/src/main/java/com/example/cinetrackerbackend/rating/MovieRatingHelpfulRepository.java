package com.example.cinetrackerbackend.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MovieRatingHelpfulRepository extends JpaRepository<MovieRatingHelpful, Long> {
    Optional<MovieRatingHelpful> findByRatingIdAndUserId(Long ratingId, Long userId);
    void deleteByRatingIdAndUserId(Long ratingId, Long userId);
    boolean existsByRatingIdAndUserId(Long ratingId, Long userId);
    void deleteByRatingId(Long ratingId);
}
