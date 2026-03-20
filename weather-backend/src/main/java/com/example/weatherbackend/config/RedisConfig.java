package com.example.weatherbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory){


        RedisCacheConfiguration weatherCache=RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))//time to live
                .serializeValuesWith
                        (RedisSerializationContext.SerializationPair.fromSerializer(
                            RedisSerializer.json()
                        )
                        );


        RedisCacheConfiguration forecastCache=RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith
                        (RedisSerializationContext.SerializationPair.fromSerializer(
                            RedisSerializer.json()
                        )
                        );

        Map<String,RedisCacheConfiguration> configs=new HashMap<>();
        configs.put("weather",weatherCache);
        configs.put("forecast",forecastCache);


        return RedisCacheManager.
                builder(redisConnectionFactory)
                .withInitialCacheConfigurations(configs).build();
    }
}
