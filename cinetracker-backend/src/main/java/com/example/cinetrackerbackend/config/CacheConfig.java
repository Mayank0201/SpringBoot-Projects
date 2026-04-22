package com.example.cinetrackerbackend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.time.Duration;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.Cache;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig implements CachingConfigurer {

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory factory){
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofHours(1));

    RedisCacheConfiguration ratingSummaryConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(30));

    RedisCacheConfiguration ratingSummariesBatchConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(30));

    RedisCacheConfiguration genresConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofHours(2));

    RedisCacheConfiguration searchMoviesConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(15));

    RedisCacheConfiguration popularMoviesConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(30));

    RedisCacheConfiguration moviesByGenreConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(30));

    RedisCacheConfiguration movieDetailsConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofHours(2));

    return RedisCacheManager.builder(factory)
      .cacheDefaults(defaultConfig)
      .withCacheConfiguration("ratingSummary", ratingSummaryConfig)
      .withCacheConfiguration("ratingSummariesBatch", ratingSummariesBatchConfig)
      .withCacheConfiguration("genres", genresConfig)
      .withCacheConfiguration("searchMovies", searchMoviesConfig)
      .withCacheConfiguration("popularMovies", popularMoviesConfig)
      .withCacheConfiguration("moviesByGenre", moviesByGenreConfig)
      .withCacheConfiguration("movieDetails", movieDetailsConfig)
      .build();
  }

  @Override
  public CacheErrorHandler errorHandler() {
    return new SimpleCacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.error("Cache GET error for key {}: {}", key, exception.getMessage());
      }

      @Override
      public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.error("Cache PUT error for key {}: {}", key, exception.getMessage());
      }

      @Override
      public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.error("Cache EVICT error for key {}: {}", key, exception.getMessage());
      }

      @Override
      public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.error("Cache CLEAR error for cache {}: {}", cache.getName(), exception.getMessage());
      }
    };
  }
}
