package com.example.cinetrackerbackend.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final MovieRatingRepository ratingRepository;

    @Transactional
    public MovieRating setRating(Long movieId, Long userId, Double rating) {
        if (rating == null || rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }
        
        if ((rating * 2) % 1 != 0) {
            throw new IllegalArgumentException("Rating must be in 0.5 increments (1.0, 1.5, 2.0, etc.)");
        }

        Optional<MovieRating> existingRating = ratingRepository.findByMovieIdAndUserId(movieId, userId);

        MovieRating movieRating;
        if (existingRating.isPresent()) {
            movieRating = existingRating.get();
            movieRating.setRating(rating);
        } else {
            movieRating = new MovieRating();
            movieRating.setMovieId(movieId);
            movieRating.setUserId(userId);
            movieRating.setRating(rating);
        }

        return ratingRepository.save(movieRating);
    }

    @Transactional
    public void deleteRating(Long movieId, Long userId) {
        ratingRepository.deleteByMovieIdAndUserId(movieId, userId);
    }

    public RatingSummaryDTO getRatingSummary(Long movieId, Long userId) {
        RatingSummaryProjection summary = ratingRepository.getRatingSummary(movieId);
        Optional<MovieRating> userRating = ratingRepository.findByMovieIdAndUserId(movieId, userId);

        return new RatingSummaryDTO(
            movieId,
            summary.getAverageRating(),
            summary.getRatingCount(),
            userRating.map(MovieRating::getRating).orElse(null)
        );
    }

    public RatingSummaryDTO getRatingSummary(Long movieId) {
        RatingSummaryProjection summary = ratingRepository.getRatingSummary(movieId);

        return new RatingSummaryDTO(
            movieId,
            summary.getAverageRating(),
            summary.getRatingCount(),
            null
        );
    }
}
