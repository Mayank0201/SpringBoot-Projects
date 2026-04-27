package com.example.cinetrackerbackend.watchlist;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistResponse {

  private Long id;
  private Long movieId;
  private String title;
  private String posterUrl;
  private String overview;
  private Double rating;
  private Double voteAverage;
  private String releaseDate;
  private int releaseYear;
  private String genre;
  private String status;

}


