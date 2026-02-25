package com.example.weatherbackend.external;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ForecastApiResponse {

    // List of forecast entries returned by OpenWeather
    private List<ForecastEntry> list;

    @Getter
    @Setter
    public static class ForecastEntry {
    //structured according to the api response
        private String dt_txt;

        private Main main;

        private List<Weather> weather;
    }

    @Getter
    @Setter
    public static class Main {
        private Double temp;
    }

    @Getter
    @Setter
    public static class Weather {
        private String description;
    }
}