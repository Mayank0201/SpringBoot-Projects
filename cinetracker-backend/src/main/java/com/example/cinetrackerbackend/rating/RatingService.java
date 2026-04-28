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
    private final com.example.cinetrackerbackend.user.UserRepository userRepository;
    private final com.example.cinetrackerbackend.movie.MovieRepository movieRepository;
    private final com.example.cinetrackerbackend.movie.TmdbClient tmdbClient;
    private final com.example.cinetrackerbackend.gamification.GamificationService gamificationService;
    private final MovieRatingHelpfulRepository helpfulRepository;


    @Transactional
    @CacheEvict(value = {"userRating", "ratingSummary", "ratingSummariesBatch"}, allEntries = true)
    public MovieRating setRating(Long movieId, Long userId, Double rating, String comment) {
        if (rating == null || rating < 1.0 || rating > 5.0) {
            throw new ApiException("Rating must be between 1.0 and 5.0", HttpStatus.BAD_REQUEST);
        }

        if (com.example.cinetrackerbackend.common.ContentModerator.containsSlur(comment)) {
            throw new ApiException("Contains offensive slurs. Please refrain from using them and be respectful.", HttpStatus.BAD_REQUEST);
        }

        if ((rating * 2) % 1 != 0) {
            throw new ApiException("Rating must be in 0.5 increments", HttpStatus.BAD_REQUEST);
        }

        
        // Ensure movie exists in local movie table for foreign key constraint
        ensureMovieExists(movieId);

        try {
            Optional<MovieRating> existingRating = ratingRepository.findByMovieIdAndUser_Id(movieId, userId);

            MovieRating movieRating;
            boolean isNew = existingRating.isEmpty();
            if (!isNew) {
                movieRating = existingRating.get();
                movieRating.setRating(rating);
                movieRating.setComment(comment);
                // Reset helpful count when review is edited
                movieRating.setHelpfulCount(0L);
                // Delete existing helpful records for this rating
                helpfulRepository.deleteByRatingId(movieRating.getId());
            } else {
                com.example.cinetrackerbackend.user.User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
                
                movieRating = new MovieRating();
                movieRating.setMovieId(movieId);
                movieRating.setUser(user);
                movieRating.setRating(rating);
                movieRating.setComment(comment);
            }

            MovieRating saved = ratingRepository.save(movieRating);
            
            // Award XP only for new ratings or first reviews
            if (isNew) {
                gamificationService.awardXpWithLimit(userId, com.example.cinetrackerbackend.gamification.GamificationService.XP_RATE_MOVIE, "RATE", "Rated movie ID: " + movieId);
            }



            return saved;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error saving rating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @Transactional
    @CacheEvict(value = {"userRating", "ratingSummary", "ratingSummariesBatch"}, allEntries = true)
    public void deleteRating(Long movieId, Long userId) {
        try {
            ratingRepository.deleteByMovieIdAndUser_Id(movieId, userId);
        } catch (Exception e) {
            throw new ApiException("Error deleting rating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Cacheable(value = "ratingSummary", key = "#movieId + '-' + #userId")
    public RatingSummaryDTO getRatingSummary(Long movieId, Long userId) {
        try {
            RatingSummaryProjection summary = ratingRepository.getRatingSummary(movieId);
            Optional<MovieRating> userRating = ratingRepository.findByMovieIdAndUser_Id(movieId, userId);

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

    public org.springframework.data.domain.Page<ReviewDTO> getMovieReviews(Long movieId, Long currentUserId, int page, int size, String sortBy) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by("createdAt").descending();
        if ("helpful".equalsIgnoreCase(sortBy)) {
            sort = org.springframework.data.domain.Sort.by("helpfulCount").descending().and(org.springframework.data.domain.Sort.by("createdAt").descending());
        }
        
        org.springframework.data.domain.Page<MovieRating> ratings = ratingRepository.findReviewsWithComments(movieId, org.springframework.data.domain.PageRequest.of(page - 1, size, sort));
        
        return ratings.map(rating -> {
            boolean isHelpful = false;
            if (currentUserId != null) {
                isHelpful = helpfulRepository.existsByRatingIdAndUserId(rating.getId(), currentUserId);
            }
            return new ReviewDTO(rating, isHelpful);
        });
    }

    public org.springframework.data.domain.Page<MovieRating> getUserReviews(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException("User not found", HttpStatus.NOT_FOUND);
        }
        return ratingRepository.findByUser_IdOrderByCreatedAtDesc(userId, org.springframework.data.domain.PageRequest.of(page - 1, size));
    }

    @Transactional
    public void toggleHelpful(Long ratingId, Long userId) {
        MovieRating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new ApiException("Rating not found", HttpStatus.NOT_FOUND));

        Optional<MovieRatingHelpful> existing = helpfulRepository.findByRatingIdAndUserId(ratingId, userId);

        if (existing.isPresent()) {
            helpfulRepository.delete(existing.get());
            rating.setHelpfulCount(Math.max(0, rating.getHelpfulCount() - 1));
        } else {
            helpfulRepository.save(new MovieRatingHelpful(ratingId, userId));
            rating.setHelpfulCount(rating.getHelpfulCount() + 1);
            
            // Award 1 XP to the review owner when someone marks it helpful
            // Don't award XP to self
            if (rating.getUser().getId() != userId) {
                gamificationService.awardXpWithLimit(rating.getUser().getId(), 1, "HELPFUL_REVIEW", "Review marked as helpful on movie: " + rating.getMovieId());
            }
        }
        ratingRepository.save(rating);
    }

    private void ensureMovieExists(Long movieId) {


        if (movieRepository.existsById(movieId)) {
            return;
        }

        try {
            java.util.Map<String, Object> movieData = tmdbClient.getMovieDetails(movieId);
            if (movieData == null || movieData.isEmpty()) {
                return;
            }

            String title = (String) movieData.get("title");
            if (title == null || title.isBlank()) {
                title = (String) movieData.get("name");
            }

            String releaseDate = (String) movieData.get("release_date");
            Integer releaseYear = null;
            if (releaseDate != null && releaseDate.length() >= 4) {
                try {
                    releaseYear = Integer.parseInt(releaseDate.substring(0, 4));
                } catch (NumberFormatException ignored) {}
            }

            java.util.List<java.util.Map<String, Object>> genres = (java.util.List<java.util.Map<String, Object>>) movieData.getOrDefault("genres", java.util.Collections.emptyList());
            String genre = genres.stream()
                .map(g -> (String) g.get("name"))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.joining(", "));

            com.example.cinetrackerbackend.movie.Movie movie = new com.example.cinetrackerbackend.movie.Movie();
            movie.setId(movieId);
            movie.setTitle(title);
            movie.setGenre(genre.isBlank() ? "N/A" : genre);
            movie.setReleaseYear(releaseYear != null ? releaseYear : 0);

            movieRepository.save(movie);
        } catch (Exception e) {
            // Log or handle error if necessary
        }
    }
}

