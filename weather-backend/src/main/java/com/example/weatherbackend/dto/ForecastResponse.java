package com.example.weatherbackend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForecastResponse {

    private String city;
    private List<ForecastItem> forecasts;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ForecastItem {
        private String dateTime;
        private Double temperature;
        private String description;
    }
}