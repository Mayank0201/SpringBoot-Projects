package com.example.weatherbackend.service;

import com.example.weatherbackend.dto.WeatherResponse;
import com.example.weatherbackend.entity.SearchHistory;
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

        WeatherApiResponse response =
                restTemplate.getForObject(url, WeatherApiResponse.class);

        assert response != null;
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
    }

    public List<SearchHistory> getAllSearches() {
        return repository.findAll();
    }
}