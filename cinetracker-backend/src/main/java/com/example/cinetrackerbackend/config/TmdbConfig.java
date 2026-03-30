package com.example.cinetrackerbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class TmdbConfig {

  @Value("${tmdb.api.key}")
  private String apiKey;

  @Value("${tmdb.api.baseUrl}")
  private String baseUrl;

}
