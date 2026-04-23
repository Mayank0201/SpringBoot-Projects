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
            int deletedCount = userRepository.clearExpiredVerificationTokens(now);
            log.info("Cleanup completed. cleared {} expired verification tokens.", deletedCount);
        } catch (Exception e) {
            log.error("Error during verification token cleanup", e);
        }
    }

    /**
     * Clean up expired refresh tokens once a day at midnight
     * Format: second minute hour day month dayOfWeek
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredRefreshTokens() {
        log.info("starting cleanup of expired refresh tokens...");
        try {
            Instant now = Instant.now();
            int clearedCount = userRepository.clearExpiredRefreshTokens(now);
            log.info("cleanup completed. cleared {} expired refresh tokens.", clearedCount);
        } catch (Exception e) {
            log.error("error during refresh token cleanup", e);
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
     * Keep-alive ping to prevent Render free tier from sleeping every 10 minutes
     */
    @Scheduled(cron = "0 */14 * * * *")
    public void keepAlive() {
        log.info("Executing keep-alive ping to prevent Render from sleeping...");
        try {
            java.net.URL url = new java.net.URL("https://springboot-projects-4m5x.onrender.com/actuator/health");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            log.info("Keep-alive ping successful. Response code: {}", responseCode);
        } catch (Exception e) {
            log.error("Error during keep-alive ping", e);
        }
    }
}