package com.example.cinetrackerbackend.watchlist;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/watchlist")
public class WatchlistController{

  private final WatchlistService watchlistService;

  @PostMapping("/add")
  public Watchlist addToWatchlist(
		  @RequestParam Long userId,
		  @RequestParam Long movieId){
    return watchlistService.addToWatchlist(userId,movieId);
  }

}
