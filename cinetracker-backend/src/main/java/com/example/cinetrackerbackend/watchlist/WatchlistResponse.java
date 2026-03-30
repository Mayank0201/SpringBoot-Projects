package com.example.cinetrackerbackend.watchlist;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor

public class WatchlistResponse{

  private Long id;

  private Long movieId;

  private String title;

  private String genre;

  private int releaseYear;

}

