package com.example.cinetrackerbackend.movie;

import com.example.cinetrackerbackend.config.TmdbConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TmdbClient {

  private final TmdbConfig tmdbConfig;
  private final RestTemplate restTemplate = new RestTemplate();

  public Map<String, Object> searchMovies(String query) {

    String url = tmdbConfig.getBaseUrl()
       + "/search/movie?api_key=" + tmdbConfig.getApiKey()
       + "&query=" + query;

    return restTemplate.getForObject(url, Map.class);
  }
}
