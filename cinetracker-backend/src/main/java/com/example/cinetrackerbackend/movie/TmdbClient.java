package com.example.cinetrackerbackend.movie;

import com.example.cinetrackerbackend.config.TmdbConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;

@Component
@RequiredArgsConstructor
public class TmdbClient {

  private final TmdbConfig tmdbConfig;
  private final RestTemplate restTemplate = new RestTemplate();

  @Cacheable("searchMovies")
  public Map<String, Object> searchMovies(String query) {

    String url = tmdbConfig.getBaseUrl()
       + "/search/movie?api_key=" + tmdbConfig.getApiKey()
       + "&query=" + query;

    return restTemplate.getForObject(url, Map.class);
  }

  @Cacheable("popularMovies")
  public Map<String, Object> getPopularMovies() {

    String url = tmdbConfig.getBaseUrl()
            + "/movie/popular?api_key=" + tmdbConfig.getApiKey();

    return restTemplate.getForObject(url, Map.class);
  }

  @Cacheable("genres")
  public Map<String,Object> getGenres(){
    String url= tmdbConfig.getBaseUrl()
            + "/genre/movie/list?api_key=" + tmdbConfig.getApiKey();
    
    return restTemplate.getForObject(url,Map.class);
  }

  @Cacheable("moviesByGenre")
  public Map<String,Object> getMoviesByGenre(Long genreId){
    String url= tmdbConfig.getBaseUrl()
            + "/discover/movie?api_key=" + tmdbConfig.getApiKey()
            + "&with_genres=" + genreId;

    return restTemplate.getForObject(url,Map.class);
  }

}
