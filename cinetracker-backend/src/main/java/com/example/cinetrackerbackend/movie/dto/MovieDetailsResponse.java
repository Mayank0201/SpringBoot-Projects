package com.example.cinetrackerbackend.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MovieDetailsResponse(
  Long id,
  Long movieId,
  Long tmdbId,
  String title,
  String name,
  String posterUrl,
  @JsonProperty("poster_path") String posterPath,
  String overview,
  Double rating,
  Double voteAverage,
  String releaseDate,
  @JsonProperty("release_date") String releaseDateSnake,
  Integer releaseYear,
  String genre,
  List<String> genreNames,
  Double averageRating,
  Long ratingCount
) {
}