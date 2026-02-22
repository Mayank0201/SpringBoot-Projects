package com.example.weatherbackend.external;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WeatherApiResponse {

    private Main main;
    private List<Weather> weather;

    @Getter
    @Setter
    public static class Main {
        private Double temp;
    }
//name of these shouls match the json keys
    @Getter
    @Setter
    public static class Weather {
        private String description;
    }
}