package com.example.weatherbackend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bucket;
import com.example.weatherbackend.config.RateLimitConfig;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig config;

    // Store buckets per IP
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitConfig config) {
        this.config = config;
    }

    private Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, k -> config.createBucket());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Better IP handling (future-proof)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        Bucket bucket = resolveBucket(ip);
        //basically like a bucket with 60 requests per minute
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");

            response.getWriter().write("""
                {
                  "status": 429,
                  "error": "Too Many Requests",
                  "message": "Rate limit exceeded. Try again later."
                }
            """);
        }
    }
}