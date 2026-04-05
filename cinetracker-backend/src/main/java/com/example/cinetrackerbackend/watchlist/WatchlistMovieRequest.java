package com.example.cinetrackerbackend.watchlist;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WatchlistMovieRequest {

  @NotNull(message = "movieId is required")
  private Long movieId;
}
