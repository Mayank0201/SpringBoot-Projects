package com.example.cinetrackerbackend.watchlist;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.cinetrackerbackend.user.User;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/watchlist")
public class WatchlistController {

  private final WatchlistService watchlistService;

  @PostMapping("/add")
  public Watchlist addToWatchlist(@RequestParam Long movieId){

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) auth.getPrincipal();

    return watchlistService.addToWatchlist(user.getId(), movieId);
  }

  @GetMapping("/get")
  public List<WatchlistResponse> getUserWatchlist(){

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) auth.getPrincipal();

    return watchlistService.getUserWatchlist(user.getId());
  }

  @DeleteMapping("/remove")
  public void removeFromWatchlist(@RequestParam Long movieId){

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) auth.getPrincipal();

    watchlistService.removeFromWatchlist(user.getId(), movieId);
  }
}