package com.example.cinetrackerbackend.discovery;

import com.example.cinetrackerbackend.common.ApiResponse;
import com.example.cinetrackerbackend.movie.TmdbClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/discovery")
@RequiredArgsConstructor
public class DiscoveryController {

    private final MoodMatcherService moodMatcherService;
    private final TmdbClient tmdbClient;

    @GetMapping("/mood-match")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMoodMatch(@RequestParam MoodMatcherService.Mood mood) {
        List<Map<String, Object>> movies = moodMatcherService.getMoviesForMood(mood);
        return ResponseEntity.ok(ApiResponse.success("Mood match complete! Enjoy your watch.", 200, movies));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrending(@RequestParam(defaultValue = "1") int page) {
        Map<String, Object> popular = tmdbClient.getPopularMovies(page);
        return ResponseEntity.ok(ApiResponse.success("Trending movies fetched", 200, popular));
    }
}
