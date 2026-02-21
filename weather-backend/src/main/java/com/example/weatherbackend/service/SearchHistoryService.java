package com.example.weatherbackend.service;

import com.example.weatherbackend.entity.SearchHistory;
import com.example.weatherbackend.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    public SearchHistory fetchAndSaveWeather(String city) {

        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + city + "&appid=" +apiKey + "&units=metric";

        Map response = restTemplate.getForObject(url, Map.class);

        Map main = (Map) response.get("main");
        Double temperature = Double.valueOf(main.get("temp").toString());

        List weatherList = (List) response.get("weather");
        Map weather = (Map) weatherList.get(0);
        String description = weather.get("description").toString();

        SearchHistory search = SearchHistory.builder()
                .city(city)
                .temperature(temperature)
                .description(description)
                .searchedAt(LocalDateTime.now())
                .build();

        return repository.save(search);
    }

    public List<SearchHistory> getAllSearches() {
        return repository.findAll();
    }
}