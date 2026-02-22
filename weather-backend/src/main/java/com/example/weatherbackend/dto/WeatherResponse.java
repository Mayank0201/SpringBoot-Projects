package com.example.weatherbackend.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeatherResponse {

    private String city;
    private Double temperature;
    private String description;
    private String searchedAt;
}