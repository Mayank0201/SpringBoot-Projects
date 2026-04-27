package com.example.cinetrackerbackend.discovery;

import com.example.cinetrackerbackend.common.ApiResponse;
import com.example.cinetrackerbackend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/discovery")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/recommendations")

    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPersonalRecommendations() {
        Long userId = getAuthenticatedUserId();
        List<Map<String, Object>> recommendations = recommendationService.getPersonalizedRecommendations(userId);
        return ResponseEntity.ok(ApiResponse.success("Recommendations fetched successfully", 200, recommendations));
    }

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof com.example.cinetrackerbackend.user.User)) {
            throw new com.example.cinetrackerbackend.exception.ApiException("Authentication required", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        com.example.cinetrackerbackend.user.User user = (com.example.cinetrackerbackend.user.User) auth.getPrincipal();
        return user.getId();
    }
}
