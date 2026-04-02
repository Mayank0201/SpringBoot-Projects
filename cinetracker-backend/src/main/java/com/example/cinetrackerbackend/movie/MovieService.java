package com.example.cinetrackerbackend.movie;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.List;

import com.example.cinetrackerbackend.movie.dto.GenreResponse;
import com.example.cinetrackerbackend.movie.dto.MovieSearchResponse;
import com.example.cinetrackerbackend.movie.dto.PaginatedResponse;
import com.example.cinetrackerbackend.movie.dto.HomeScreenMovieResponse;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService{

  private final TmdbClient tmdbClient;

  private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

  private HomeScreenMovieResponse mapToHomeMovieResponse(Map<String, Object> movie) {

    String posterPath = (String) movie.get("poster_path");

    String fullPosterUrl = posterPath != null
      ? IMAGE_BASE_URL + posterPath
      : null;

    return new HomeScreenMovieResponse(
          ((Number) movie.get("id")).longValue(),
          movie.get("title")!=null ? (String) movie.get("title") : (String) movie.get("name"),
          fullPosterUrl
        );
  }


  public PaginatedResponse<MovieSearchResponse> searchMovies(String query,int page) {
    
      Map<String,Object> response=tmdbClient.searchMovies(query,page);

      List<Map<String,Object>> results=(List<Map<String,Object>>) response.get("results");

      return new PaginatedResponse<MovieSearchResponse>(
        ((Number) response.get("page")).intValue(),
        ((Number) response.get("total_pages")).intValue(),
        results.stream()
            .map(movie-> new MovieSearchResponse(
              ((Number) movie.get("id")).longValue(),//number used since it can be Integer or Double depending on the value
              (String) movie.get("title"),
              (String) movie.get("release_date"),
            movie.get("vote_average")!=null ? ((Number) movie.get("vote_average")).doubleValue()
            : 0.0,
            movie.get("poster_path")!=null ? IMAGE_BASE_URL + (String) movie.get("poster_path") : null
          )).collect(Collectors.toList()));

  }

  public PaginatedResponse<HomeScreenMovieResponse> getPopularMovies(int page){

    Map<String,Object> response=tmdbClient.getPopularMovies(page);

    List<Map<String,Object>> results=(List<Map<String,Object>>) response.get("results");
    
    return new PaginatedResponse<HomeScreenMovieResponse>(
      ((Number) response.get("page")).intValue(),
      ((Number) response.get("total_pages")).intValue(),
      results.stream()
          .map(this::mapToHomeMovieResponse)
          .collect(Collectors.toList())
    );
  }

  public List<GenreResponse> getGenres(){
    Map<String,Object> response=tmdbClient.getGenres();
    
    List<Map<String,Object>> genres=(List<Map<String,Object>>) response.get("genres");

    return genres.stream()
        .map(genre->new GenreResponse(
          ((Number) genre.get("id")).longValue(),
          (String) genre.get("name")
        )).collect(Collectors.toList());
  }

  public PaginatedResponse<HomeScreenMovieResponse> getMoviesByGenre(Long genreId,int page){
    Map<String,Object> response=tmdbClient.getMoviesByGenre(genreId,page);

    List<Map<String,Object>> results=(List<Map<String,Object>>) response.get("results");

    return new PaginatedResponse<HomeScreenMovieResponse>(
      ((Number) response.get("page")).intValue(),
      ((Number) response.get("total_pages")).intValue(),
      results.stream()
        .map(this::mapToHomeMovieResponse)
        .collect(Collectors.toList())
    );
  }
  
}
