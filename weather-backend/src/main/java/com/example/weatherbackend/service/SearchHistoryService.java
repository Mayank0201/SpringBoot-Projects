package com.example.weatherbackend.service;

import com.example.weatherbackend.dto.ForecastResponse;
import com.example.weatherbackend.dto.WeatherResponse;
import com.example.weatherbackend.entity.SearchHistory;
import com.example.weatherbackend.exception.CityNotFoundException;
import com.example.weatherbackend.external.ForecastApiResponse;
import com.example.weatherbackend.external.WeatherApiResponse;
import com.example.weatherbackend.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SearchHistoryService {

    private final SearchHistoryRepository repository;
    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    public SearchHistoryService(SearchHistoryRepository repository,
                                RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    public WeatherResponse fetchAndSaveWeather(String city) {

        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + city + "&appid=" + apiKey + "&units=metric";

        try {
            WeatherApiResponse response =
                    restTemplate.getForObject(url, WeatherApiResponse.class);

            Double temperature = response.getMain().getTemp();
            String description = response.getWeather().get(0).getDescription();

            SearchHistory search = SearchHistory.builder()
                    .city(city)
                    .temperature(temperature)
                    .description(description)
                    .searchedAt(LocalDateTime.now())
                    .build();

            SearchHistory saved = repository.save(search);

            return WeatherResponse.builder()
                    .city(saved.getCity())
                    .temperature(saved.getTemperature())
                    .description(saved.getDescription())
                    .searchedAt(saved.getSearchedAt().toString())
                    .build();

        } catch (Exception e) {
            throw new CityNotFoundException("City not found: " + city);
        }
    }

    public ForecastResponse fetchForecast(String city) {

        String url = "https://api.openweathermap.org/data/2.5/forecast?q="
                + city + "&appid=" + apiKey + "&units=metric";

        try {

            ForecastApiResponse response =
                    restTemplate.getForObject(url, ForecastApiResponse.class);

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

            return ForecastResponse.builder()
                    .city(city)
                    .forecasts(items)
                    .build();

        } catch (Exception e) {
            throw new CityNotFoundException("City not found: " + city);
        }
    }

    public List<SearchHistory> getAllSearches() {
        return repository.findAll();
    }
}