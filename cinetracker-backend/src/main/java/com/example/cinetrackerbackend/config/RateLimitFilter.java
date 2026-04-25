package com.example.cinetrackerbackend.config;

import com.example.cinetrackerbackend.security.JwtService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();
    private final JwtService jwtService;

    private Bucket createNewBucket() {
        // Increase to 60 requests per minute (1 per second average)
        Bandwidth limit = Bandwidth.simple(60, Duration.ofMinutes(1));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket resolveBucket(String key) {
        return bucketCache.computeIfAbsent(key, k -> createNewBucket());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String key;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                key = jwtService.extractUsername(token);
            } else {
                key = request.getRemoteAddr();
            }
        } catch (Exception e) {
            key = request.getRemoteAddr(); 
        }

        Bucket bucket = resolveBucket(key);
        
        // Add rate limit headers to the response
        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            
            // X-Rate-Limit-Retry-After-Seconds header
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", "2"); 
            response.getWriter().write("{\"success\":false,\"message\":\"Too many requests. Please try again in a few seconds.\",\"status\":429,\"timestamp\":null,\"data\":null}");
        }
    }
}