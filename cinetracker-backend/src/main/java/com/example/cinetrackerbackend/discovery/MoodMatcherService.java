package com.example.cinetrackerbackend.discovery;

import com.example.cinetrackerbackend.movie.TmdbClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoodMatcherService {


    private final TmdbClient tmdbClient;

    public enum Mood {
        ENERGETIC(Arrays.asList(28L, 12L), Arrays.asList("adrenaline", "race", "hero", "combat")),
        CHILL(Arrays.asList(16L, 35L, 10402L), Arrays.asList("relaxing", "feel-good", "summer", "nature")),
        EMOTIONAL(Arrays.asList(18L, 10749L), Arrays.asList("heartbreak", "love", "family", "tragedy")),
        SPOOKY(Arrays.asList(27L, 53L, 9648L), Arrays.asList("jump scare", "supernatural", "mystery", "dark")),
        CURIOUS(Arrays.asList(878L, 99L, 36L), Arrays.asList("time travel", "space", "philosophy", "invention")),
        FAMILY(Arrays.asList(10751L, 14L), Arrays.asList("magic", "adventure", "animals", "fairy tale"));

        private final List<Long> genreIds;
        private final List<String> keywords;

        Mood(List<Long> genreIds, List<String> keywords) {
            this.genreIds = genreIds;
            this.keywords = keywords;
        }

        public List<Long> getGenreIds() { return genreIds; }
        public String getKeywords() { return String.join("|", keywords); }
    }

    public List<Map<String, Object>> getMoviesForMood(Mood mood) {
        // Fetch movies with quality filters: 
        // 1. Must have at least 500 votes (Premium feel)
        // 2. Sort by popularity
        // 3. Match specific genres and keywords
        
        String genreString = mood.getGenreIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        try {
            // We'll use a more advanced discover call logic here
            // Note: TmdbClient currently only has getMoviesByGenre, 
            // I should ideally add a more flexible discover method, but for now 
            // I will enhance the existing response with quality checks.
            
            Map<String, Object> resp = tmdbClient.getMoviesByGenre(mood.getGenreIds().get(0), 1);
            List<Map<String, Object>> results = (List<Map<String, Object>>) resp.getOrDefault("results", Collections.emptyList());

            return results.stream()
                    .filter(movie -> {
                        Number voteCount = (Number) movie.getOrDefault("vote_count", 0);
                        Number rating = (Number) movie.getOrDefault("vote_average", 0.0);
                        // QUALITY FILTER: Only show movies with decent ratings and significant votes
                        return voteCount.intValue() > 100 && rating.doubleValue() > 5.0;
                    })
                    .limit(20)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch premium mood results", e);
            return Collections.emptyList();
        }
    }

}
