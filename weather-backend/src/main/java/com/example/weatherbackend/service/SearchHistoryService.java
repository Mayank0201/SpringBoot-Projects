package com.example.weatherbackend.service;

import com.example.weatherbackend.client.WeatherClient;
import com.example.weatherbackend.dto.ForecastResponse;
import com.example.weatherbackend.dto.WeatherResponse;
import com.example.weatherbackend.entity.SearchHistory;
import com.example.weatherbackend.exception.CityNotFoundException;
import com.example.weatherbackend.external.ForecastApiResponse;
import com.example.weatherbackend.external.WeatherApiResponse;
import com.example.weatherbackend.repository.SearchHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.List;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

@Service
@Slf4j //for logging
public class SearchHistoryService {

    private final SearchHistoryRepository repository;
    private final WeatherClient weatherClient;
    private final MeterRegistry meterRegistry;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    @Value("${weather.api.key}")
    private String apiKey;

    public SearchHistoryService(SearchHistoryRepository repository,
                                WeatherClient weatherClient,MeterRegistry meterRegistry) {
        this.repository = repository;
        this.weatherClient = weatherClient;
        this.meterRegistry=meterRegistry;
        this.cacheHitCounter = meterRegistry.counter("weather.cache.hits");
        this.cacheMissCounter = meterRegistry.counter("weather.cache.misses");
    }

    @Cacheable(value= "weather",key = "#city.toLowerCase().trim()",unless = "#result==null")
    public WeatherResponse fetchAndSaveWeather(String city) {

        cacheMissCounter.increment();//means cache not used and method called
        //String url = "https://api.openweathermap.org/data/2.5/weather?q="
         //       + city + "&appid=" + apiKey + "&units=metric";

        try {
            WeatherApiResponse response =
                    weatherClient.fetchCurrentWeather(city);
            meterRegistry.counter(
                    "weather.api.calls",
                    "city", city.toLowerCase(),
                    "status", "success"
            ).increment();

            Double temperature = response.getMain().getTemp();
            String description = response.getWeather().get(0).getDescription();

            SearchHistory search = SearchHistory.builder()
                    .city(city)
                    .temperature(temperature)
                    .description(description)
                    .searchedAt(LocalDateTime.now())
                    .build();

            SearchHistory saved = repository.save(search);

            log.info("Fetching weather from external API for city: {}",city);
            //if no log , then redis used the cached value
            return WeatherResponse.builder()
                    .city(saved.getCity())
                    .temperature(saved.getTemperature())
                    .description(saved.getDescription())
                    .searchedAt(saved.getSearchedAt().toString())
                    .build();

        } catch (Exception e) {
            meterRegistry.counter("weather.api.calls","city",city.toLowerCase().trim(),
                    "status","failure").increment();
            throw new CityNotFoundException("City not found: " + city);
        }
    }

    @Cacheable(value = "forecast", key = "#city.toLowerCase().trim()", unless = "#result == null")
    public ForecastResponse fetchForecast(String city) {

        cacheMissCounter.increment();

        try {
            ForecastApiResponse response =
                    weatherClient.fetchForecast(city);

            meterRegistry.counter(
                    "forecast.api.calls",
                    "city", city.toLowerCase(),
                    "status", "success"
            ).increment();

            // Convert external API model → internal DTO model
            var items = response.getList()
                    .stream()
                    .limit(5) // only first 5 entries
                    .map(entry -> ForecastResponse.ForecastItem.builder()
                            .dateTime(entry.getDt_txt())
                            .temperature(entry.getMain().getTemp())
                            .description(entry.getWeather().get(0).getDescription())
                            .build())
                    .toList();

            log.info("Fetching forecast from external API for city: {}", city);

            return ForecastResponse.builder()
                    .city(city)
                    .forecasts(items)
                    .build();

        } catch (Exception e) {
            meterRegistry.counter("forecast.api.calls", "city", city.toLowerCase().trim(),
                    "status", "failure").increment();
            throw new CityNotFoundException("City not found: " + city);
        }
    }

    public List<SearchHistory> getAllSearches() {
        return repository.findAll();
    }
}