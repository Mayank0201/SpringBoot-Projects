package com.example.cinetrackerbackend.config;

import com.example.cinetrackerbackend.security.JwtService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    private final JwtService jwtService;

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.simple(40, Duration.ofMinutes(1));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket resolveBucket(String key) {
        return bucketCache.computeIfAbsent(key, k -> createNewBucket());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");

        String key;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                key = jwtService.extractUsername(token);
            } else {
                key = httpRequest.getRemoteAddr();
            }
        } catch (Exception e) {
            key = httpRequest.getRemoteAddr(); 
        }

        Bucket bucket = resolveBucket(key);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"success\":false,\"message\":\"Too many requests\",\"status\":429,\"timestamp\":null,\"data\":null}");
        }
    }
}