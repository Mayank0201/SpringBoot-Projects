package com.example.cinetrackerbackend.movie.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class HomeScreenMovieResponse{

  private Long id;
  private String name;
  private String posterUrl;

}
