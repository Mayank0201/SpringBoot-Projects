package com.example.weatherbackend.service;

import com.example.weatherbackend.entity.SearchHistory;
import com.example.weatherbackend.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SearchHistoryService {

    private final SearchHistoryRepository repository;

    public SearchHistoryService(SearchHistoryRepository repository) {
        this.repository = repository;
    }

    public SearchHistory saveSearch(String city, Double temperature, String description) {

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