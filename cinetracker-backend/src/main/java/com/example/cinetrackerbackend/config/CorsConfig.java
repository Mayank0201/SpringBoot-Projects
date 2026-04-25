package com.example.cinetrackerbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow all origins for mobile/web flexibility
        // In a strict production environment, you might want to limit this
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control"
        ));
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        config.setAllowCredentials(true);
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Rate-Limit-Remaining",
            "X-Rate-Limit-Retry-After-Seconds"
        ));
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
