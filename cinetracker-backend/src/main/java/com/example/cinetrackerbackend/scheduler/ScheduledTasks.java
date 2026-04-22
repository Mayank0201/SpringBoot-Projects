package com.example.cinetrackerbackend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.movie.MovieService;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final UserRepository userRepository;
    private final MovieService movieService;

    /**
     * Clean up expired email verification tokens every 60 minutes
     * Format: second minute hour day month dayOfWeek
     */
    @Scheduled(cron = "0 */60 * * * *")
    public void cleanupExpiredVerificationTokens() {
        log.info("Starting cleanup of expired email verification tokens...");
        try {
            Instant now = Instant.now();
            int deletedCount = userRepository.deleteByEmailVerificationTokenExpiresAtBefore(now);
            log.info("Cleanup completed. Deleted {} expired verification tokens.", deletedCount);
        } catch (Exception e) {
            log.error("Error during verification token cleanup", e);
        }
    }

    /**
     * Refresh TMDB genre cache every 30 minutes
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void refreshTmdbGenreCache() {
        log.info("Starting TMDB genre cache refresh...");
        try {
            movieService.initializeGenreCache();
            log.info("TMDB genre cache refreshed successfully");
        } catch (Exception e) {
            log.error("Error during genre cache refresh", e);
        }
    }

    /**
     * Health check log every 10 minutes
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void hourlyHealthCheck() {
        log.debug("Health check: Application is running normally");
    }
}