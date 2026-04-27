package com.example.cinetrackerbackend.gamification;


import com.example.cinetrackerbackend.common.ApiResponse;
import com.example.cinetrackerbackend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping("/challenges")
    public ResponseEntity<ApiResponse<List<ChallengeService.Challenge>>> getDailyChallenges(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(401).build();
        }
        
        List<ChallengeService.Challenge> challenges = challengeService.getDailyChallenges(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Daily challenges fetched", 200, challenges));
    }

    @GetMapping("/quests")
    public ResponseEntity<ApiResponse<List<ChallengeService.Challenge>>> getQuests(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ChallengeService.Challenge> quests = challengeService.getQuests(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Quests fetched", 200, quests));
    }

    @PostMapping("/quests/{questId}/claim")
    public ResponseEntity<ApiResponse<Void>> claimQuest(
            @PathVariable String questId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String message = challengeService.claimQuest(user.getId(), questId);
        return ResponseEntity.ok(ApiResponse.success(message, 200, null));

    }
}

