package com.example.cinetrackerbackend.movie;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import com.example.cinetrackerbackend.rating.RatingService;
import com.example.cinetrackerbackend.rating.RatingSummaryDTO;

import com.example.cinetrackerbackend.movie.dto.GenreResponse;
import com.example.cinetrackerbackend.movie.dto.MovieSearchResponse;
import com.example.cinetrackerbackend.movie.dto.PaginatedResponse;
import com.example.cinetrackerbackend.movie.dto.HomeScreenMovieResponse;
import com.example.cinetrackerbackend.movie.dto.MovieDetailsResponse;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService{

  private final TmdbClient tmdbClient;
  private final RatingService ratingService;

  private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

  private HomeScreenMovieResponse mapToHomeMovieResponse(Map<String, Object> movie, Map<Long, String> genreLookup) {

    String posterPath = (String) movie.get("poster_path");
    String title = movie.get("title") != null
      ? (String) movie.get("title")
      : (String) movie.get("name");

    String fullPosterUrl = posterPath != null
      ? IMAGE_BASE_URL + posterPath
      : null;

    String releaseDate = (String) movie.get("release_date");
    Double rating = extractRating(movie);
    List<String> genreNames = extractGenreNames(movie, genreLookup);
    String genre = genreNames.isEmpty() ? "N/A" : String.join(", ", genreNames);
        Long tmdbId = ((Number) movie.get("id")).longValue();
        RatingSummaryDTO summary = ratingService.getRatingSummary(tmdbId);

    return new HomeScreenMovieResponse(
          tmdbId,
          title,
          title,
          fullPosterUrl,
          (String) movie.getOrDefault("overview", ""),
          rating,
          rating,
          releaseDate,
          extractReleaseYear(releaseDate),
          genre,
          genreNames,
          summary.getAverageRating(),
          summary.getRatingCount()
        );
  }

  private Double extractRating(Map<String, Object> movie) {
    return movie.get("vote_average") != null
      ? ((Number) movie.get("vote_average")).doubleValue()
      : 0.0;
  }

  private Integer extractReleaseYear(String releaseDate) {
    if (releaseDate != null && releaseDate.length() >= 4) {
      return Integer.parseInt(releaseDate.substring(0, 4));
    }
    return null;
  }

  private List<String> extractGenreNames(Map<String, Object> movie, Map<Long, String> genreLookup) {
    List<Number> genreIds = (List<Number>) movie.getOrDefault("genre_ids", Collections.emptyList());
    return genreIds.stream()
      .map(Number::longValue)
      .map(genreLookup::get)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private Map<Long, String> getGenreLookup() {
    Map<String, Object> genreResponse = tmdbClient.getGenres();
    List<Map<String, Object>> genres = (List<Map<String, Object>>) genreResponse.getOrDefault("genres", Collections.emptyList());
    Map<Long, String> lookup = new HashMap<>();

    for (Map<String, Object> genre : genres) {
      if (genre.get("id") != null && genre.get("name") != null) {
        lookup.put(((Number) genre.get("id")).longValue(), (String) genre.get("name"));
      }
    }

    return lookup;
  }


  public PaginatedResponse<MovieSearchResponse> searchMovies(String query,int page) {
    
      Map<String,Object> response=tmdbClient.searchMovies(query,page);
      Map<Long, String> genreLookup = getGenreLookup();

      List<Map<String,Object>> results=(List<Map<String,Object>>) response.get("results");

      return new PaginatedResponse<MovieSearchResponse>(
        ((Number) response.get("page")).intValue(),
        ((Number) response.get("total_pages")).intValue(),
        results.stream().map(movie -> {
            String releaseDate = (String) movie.get("release_date");
            Double rating = extractRating(movie);
            List<String> genreNames = extractGenreNames(movie, genreLookup);
            String genre = genreNames.isEmpty() ? "N/A" : String.join(", ", genreNames);
            Long tmdbId = ((Number) movie.get("id")).longValue();
            RatingSummaryDTO summary = ratingService.getRatingSummary(tmdbId);

            return new MovieSearchResponse(
              tmdbId,
              movie.get("title") != null ? (String) movie.get("title") : (String) movie.get("name"),
              movie.get("poster_path") != null ? IMAGE_BASE_URL + (String) movie.get("poster_path") : null,
              (String) movie.getOrDefault("overview", ""),
              rating,
              rating,
              releaseDate,
              extractReleaseYear(releaseDate),
              genre,
              genreNames,
              summary.getAverageRating(),
              summary.getRatingCount()
            );
          }).collect(Collectors.toList()));

  }

  public PaginatedResponse<HomeScreenMovieResponse> getPopularMovies(int page){

    Map<String,Object> response=tmdbClient.getPopularMovies(page);
    Map<Long, String> genreLookup = getGenreLookup();

    List<Map<String,Object>> results=(List<Map<String,Object>>) response.get("results");
    
    return new PaginatedResponse<HomeScreenMovieResponse>(
      ((Number) response.get("page")).intValue(),
      ((Number) response.get("total_pages")).intValue(),
      results.stream()
          .map(movie -> mapToHomeMovieResponse(movie, genreLookup))
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
    Map<Long, String> genreLookup = getGenreLookup();

    List<Map<String,Object>> results=(List<Map<String,Object>>) response.get("results");

    return new PaginatedResponse<HomeScreenMovieResponse>(
      ((Number) response.get("page")).intValue(),
      ((Number) response.get("total_pages")).intValue(),
      results.stream()
        .map(movie -> mapToHomeMovieResponse(movie, genreLookup))
        .collect(Collectors.toList())
    );
  }

  public MovieDetailsResponse getMovieDetails(Long movieId) {
    Map<String, Object> movie = tmdbClient.getMovieDetails(movieId);

    Long tmdbId = ((Number) movie.get("id")).longValue();
    String title = movie.get("title") != null
      ? (String) movie.get("title")
      : (String) movie.get("name");

    String posterPath = (String) movie.get("poster_path");
    String posterUrl = posterPath != null
      ? IMAGE_BASE_URL + posterPath
      : null;

    String releaseDate = (String) movie.get("release_date");
    Double rating = extractRating(movie);

    List<Map<String, Object>> genres = (List<Map<String, Object>>) movie.getOrDefault("genres", Collections.emptyList());
    List<String> genreNames = genres.stream()
      .map(genre -> (String) genre.get("name"))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    String genre = genreNames.isEmpty() ? "N/A" : String.join(", ", genreNames);
    RatingSummaryDTO summary = ratingService.getRatingSummary(tmdbId);

    return new MovieDetailsResponse(
      tmdbId,
      tmdbId,
      tmdbId,
      title,
      title,
      posterUrl,
      posterPath,
      (String) movie.getOrDefault("overview", ""),
      rating,
      rating,
      releaseDate,
      releaseDate,
      extractReleaseYear(releaseDate),
      genre,
      genreNames,
      summary.getAverageRating(),
      summary.getRatingCount()
    );
  }
  
}
