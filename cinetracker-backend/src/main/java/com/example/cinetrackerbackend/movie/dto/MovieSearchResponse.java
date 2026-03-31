package com.example.cinetrackerbackend.movie.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class MovieSearchResponse{

  private Long id;
  private String title;
  private String releaseDate;
  private Double rating;
  private String posterUrl;
}
