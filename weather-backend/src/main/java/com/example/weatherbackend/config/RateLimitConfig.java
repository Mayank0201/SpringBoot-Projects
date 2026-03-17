package com.example.weatherbackend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class RateLimitConfig {

    public Bucket createBucket() {

        Bandwidth limit = Bandwidth.simple(60, Duration.ofMinutes(1));
        //60 request limit per minute
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}