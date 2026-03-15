package com.example.weatherbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
//activates spring's caching system
public class WeatherBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherBackendApplication.class, args);
    }

}
