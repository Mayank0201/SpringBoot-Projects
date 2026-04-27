package com.example.cinetrackerbackend.discovery;

import com.example.cinetrackerbackend.movie.TmdbClient;
import com.example.cinetrackerbackend.rating.MovieRating;
import com.example.cinetrackerbackend.rating.MovieRatingRepository;
import com.example.cinetrackerbackend.watchlist.Watchlist;
import com.example.cinetrackerbackend.watchlist.WatchlistRepository;
import com.example.cinetrackerbackend.watchlist.WatchlistStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final MovieRatingRepository ratingRepository;
    private final WatchlistRepository watchlistRepository;
    private final TmdbClient tmdbClient;

    @Cacheable(value = "tasteBasedRecommendations", key = "#userId")
    public List<Map<String, Object>> getPersonalizedRecommendations(Long userId) {
        // 1. Identify "Seed" movies (High priority to COMPLETED, then High ratings)
        List<Long> completedSeeds = watchlistRepository.findByUser_IdAndStatus(userId, WatchlistStatus.COMPLETED, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .map(Watchlist::getMovieId)
                .collect(Collectors.toList());

        List<Long> highRatedSeeds = ratingRepository.findByUser_IdOrderByCreatedAtDesc(userId, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .filter(r -> r.getRating() >= 4.0)

                .map(MovieRating::getMovieId)
                .filter(id -> !completedSeeds.contains(id))
                .collect(Collectors.toList());

        if (completedSeeds.isEmpty() && highRatedSeeds.isEmpty()) {
            log.info("No seeds found for user {}, returning popular movies.", userId);
            Map<String, Object> popular = tmdbClient.getPopularMovies(1);
            return (List<Map<String, Object>>) popular.getOrDefault("results", Collections.emptyList());
        }

        // 2. Fetch recommendations from TMDB (Give priority to COMPLETED seeds)
        Map<Long, Integer> candidateCounts = new HashMap<>();
        Map<Long, Map<String, Object>> movieDetails = new HashMap<>();

        // Take up to 4 completed seeds and 2 high-rated seeds for a balanced profile
        List<Long> finalSeeds = new ArrayList<>();
        finalSeeds.addAll(completedSeeds.stream().limit(5).collect(Collectors.toList()));
        finalSeeds.addAll(highRatedSeeds.stream().limit(3).collect(Collectors.toList()));

        for (Long seedId : finalSeeds) {
            try {
                Map<String, Object> resp = tmdbClient.getRecommendations(seedId);
                List<Map<String, Object>> results = (List<Map<String, Object>>) resp.getOrDefault("results", Collections.emptyList());
                
                // Weight: Completed seeds give higher count (more impact)
                int weight = completedSeeds.contains(seedId) ? 2 : 1;

                for (Map<String, Object> movie : results) {
                    Long id = ((Number) movie.get("id")).longValue();
                    candidateCounts.put(id, candidateCounts.getOrDefault(id, 0) + weight);
                    movieDetails.put(id, movie);
                }
            } catch (Exception e) {
                log.warn("Failed to get recommendations for movie ID: {}", seedId);
            }
        }


        // 3. Filter out movies already in watchlist or already rated
        Set<Long> alreadyInteracted = new HashSet<>();
        watchlistRepository.findByUser_Id(userId, org.springframework.data.domain.Pageable.unpaged())
                .getContent().forEach(w -> alreadyInteracted.add(w.getMovieId()));
        ratingRepository.findByUser_IdOrderByCreatedAtDesc(userId, org.springframework.data.domain.Pageable.unpaged())
                .getContent().forEach(r -> alreadyInteracted.add(r.getMovieId()));


        // 4. Rank and return top 20
        List<Map<String, Object>> recommendations = candidateCounts.entrySet().stream()
                .filter(entry -> !alreadyInteracted.contains(entry.getKey()))
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(20)
                .map(entry -> movieDetails.get(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Fallback: If no recommendations found, return popular
        if (recommendations.isEmpty()) {
            log.info("No personalized recommendations found for user {}, falling back to popular.", userId);
            Map<String, Object> popular = tmdbClient.getPopularMovies(1);
            if (popular != null && popular.containsKey("results")) {
                return (List<Map<String, Object>>) popular.get("results");
            }
            return Collections.emptyList();
        }

        return recommendations;
    }
}


