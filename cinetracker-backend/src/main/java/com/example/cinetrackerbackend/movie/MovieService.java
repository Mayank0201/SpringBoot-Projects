package com.example.cinetrackerbackend.movie;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MovieService{

  private final TmdbClient tmdbClient;

  public Map<String, Object> searchMovies(String query) {
      return tmdbClient.searchMovies(query);
  }

}
