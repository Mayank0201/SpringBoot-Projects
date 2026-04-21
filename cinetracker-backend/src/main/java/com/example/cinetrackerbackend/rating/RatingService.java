package com.example.cinetrackerbackend.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.util.Optional;
import com.example.cinetrackerbackend.exception.ApiException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final MovieRatingRepository ratingRepository;

    @Transactional
    @CacheEvict(value = {"userRating", "ratingSummary", "ratingSummariesBatch"}, allEntries = true)
    public MovieRating setRating(Long movieId, Long userId, Double rating) {
        if (rating == null || rating < 1.0 || rating > 5.0) {
            throw new ApiException("Rating must be between 1.0 and 5.0", HttpStatus.BAD_REQUEST);
        }
        
        if ((rating * 2) % 1 != 0) {
            throw new ApiException("Rating must be in 0.5 increments", HttpStatus.BAD_REQUEST);
        }

        try {
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
        } catch (Exception e) {
            throw new ApiException("Error saving rating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @CacheEvict(value = {"userRating", "ratingSummary", "ratingSummariesBatch"}, allEntries = true)
    public void deleteRating(Long movieId, Long userId) {
        try {
            ratingRepository.deleteByMovieIdAndUserId(movieId, userId);
        } catch (Exception e) {
            throw new ApiException("Error deleting rating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Cacheable(value = "ratingSummary", key = "#movieId + '-' + #userId")
    public RatingSummaryDTO getRatingSummary(Long movieId, Long userId) {
        try {
            RatingSummaryProjection summary = ratingRepository.getRatingSummary(movieId);
            Optional<MovieRating> userRating = ratingRepository.findByMovieIdAndUserId(movieId, userId);

            return new RatingSummaryDTO(
                movieId,
                summary != null ? summary.getAverageRating() : 0.0,
                summary != null ? summary.getRatingCount() : 0L,
                userRating.map(MovieRating::getRating).orElse(null)
            );
        } catch (Exception e) {
            return new RatingSummaryDTO(movieId, 0.0, 0L, null);
        }
    }

    @Cacheable(value = "ratingSummary", key = "#movieId")
    public RatingSummaryDTO getRatingSummary(Long movieId) {
        try {
            RatingSummaryProjection summary = ratingRepository.getRatingSummary(movieId);

            return new RatingSummaryDTO(
                movieId,
                summary != null ? summary.getAverageRating() : 0.0,
                summary != null ? summary.getRatingCount() : 0L,
                null
            );
        } catch (Exception e) {
            return new RatingSummaryDTO(movieId, 0.0, 0L, null);
        }
    }

    @Cacheable(value = "ratingSummariesBatch", key = "#movieIds.hashCode()")
    public java.util.Map<Long, RatingSummaryDTO> getRatingSummariesForMovies(java.util.List<Long> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            java.util.List<RatingSummaryProjection> projections = ratingRepository.getRatingSummariesForMovies(movieIds);
            java.util.Map<Long, RatingSummaryDTO> result = new java.util.HashMap<>();
            
            if (projections != null) {
                for (RatingSummaryProjection proj : projections) {
                    result.put(proj.getMovieId(), new RatingSummaryDTO(
                        proj.getMovieId(),
                        proj.getAverageRating() != null ? proj.getAverageRating() : 0.0,
                        proj.getRatingCount() != null ? proj.getRatingCount() : 0L,
                        null
                    ));
                }
            }
            
            for (Long movieId : movieIds) {
                result.putIfAbsent(movieId, new RatingSummaryDTO(movieId, 0.0, 0L, null));
            }
            return result;
        } catch (Exception e) {
            java.util.Map<Long, RatingSummaryDTO> fallback = new java.util.HashMap<>();
            for (Long movieId : movieIds) {
                fallback.put(movieId, new RatingSummaryDTO(movieId, 0.0, 0L, null));
            }
            return fallback;
        }
    }
}

