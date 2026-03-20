package com.example.weatherbackend.controller;

import com.example.weatherbackend.dto.ForecastResponse;
import com.example.weatherbackend.dto.WeatherResponse;
import com.example.weatherbackend.entity.SearchHistory;
import com.example.weatherbackend.service.SearchHistoryService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class SearchHistoryController {

    private final SearchHistoryService service;

    @PostMapping("/weather")
    public WeatherResponse getWeather(
            @RequestParam
            @NotBlank(message="City cannot be empty")
            String city
    ) {
        return service.fetchAndSaveWeather(city);
    }

    // Get all search history
    @GetMapping("/history")
    public List<SearchHistory> getAllSearches() {
        return service.getAllSearches();
    }

    @GetMapping("/forecast")
    public ForecastResponse getForecast(
            @RequestParam
            @NotBlank(message = "City cannot be empty")
            String city
    ) {
        return service.fetchForecast(city);
    }
}