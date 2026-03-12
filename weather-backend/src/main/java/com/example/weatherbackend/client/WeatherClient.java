package com.example.weatherbackend.client;

import com.example.weatherbackend.external.ForecastApiResponse;
import com.example.weatherbackend.external.WeatherApiResponse;
import com.example.weatherbackend.exception.CityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherClient {

    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WeatherApiResponse fetchCurrentWeather(String city) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + city + "&appid=" + apiKey + "&units=metric";

        try {
            return restTemplate.getForObject(url, WeatherApiResponse.class);
        } catch (Exception e) {
            throw new CityNotFoundException("City not found: " + city);
        }
    }

    public ForecastApiResponse fetchForecast(String city) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?q="
                + city + "&appid=" + apiKey + "&units=metric";

        try {
            return restTemplate.getForObject(url, ForecastApiResponse.class);
        } catch (Exception e) {
            throw new CityNotFoundException("City not found: " + city);
        }
    }
}