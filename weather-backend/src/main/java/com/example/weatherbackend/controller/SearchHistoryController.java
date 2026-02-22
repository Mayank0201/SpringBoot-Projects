package com.example.weatherbackend.controller;

import com.example.weatherbackend.dto.WeatherResponse;
import com.example.weatherbackend.entity.SearchHistory;
import com.example.weatherbackend.service.SearchHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchHistoryController {

    private final SearchHistoryService service;

    public SearchHistoryController(SearchHistoryService service) {
        this.service = service;
    }

    @PostMapping("/weather")
    public WeatherResponse getWeather(@RequestParam String city) {
        return service.fetchAndSaveWeather(city);
    }

    // Get all search history
    @GetMapping("/history")
    public List<SearchHistory> getAllSearches() {
        return service.getAllSearches();
    }
}