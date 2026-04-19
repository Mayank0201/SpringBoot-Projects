package com.example.cinetrackerbackend.movie.dto;

import java.util.List;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class MovieSearchResponse{

  private Long id;

  private String title;

  private String posterUrl;

  private String overview;

  private Double rating;

  private Double voteAverage;

  private String releaseDate;

  private Integer releaseYear;

  private String genre;

  private List<String> genreNames;

  private Double averageRating;

  private Long ratingCount;
}
