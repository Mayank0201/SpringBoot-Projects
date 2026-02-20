package com.example.weatherbackend.controller;

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

    // Save new search (temporary test endpoint)
    @PostMapping("/search")
    public SearchHistory saveSearch(
            @RequestParam String city,
            @RequestParam Double temperature,
            @RequestParam String description
    ) {
        return service.saveSearch(city, temperature, description);
    }

    // Get all search history
    @GetMapping("/history")
    public List<SearchHistory> getAllSearches() {
        return service.getAllSearches();
    }
}