package com.example.cinetrackerbackend.watchlist;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.cinetrackerbackend.user.User;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/watchlist")
public class WatchlistController{

  private final WatchlistService watchlistService;

  @PostMapping("/add")
  public Watchlist addToWatchlist(
		  @RequestParam Long movieId){

    Authentication auth=SecurityContextHolder.getContext().getAuthentication();
    User user=(User) auth.getPrincipal();
    //inside jwtfiter class, we passed user object as principal isnide PasswordAuthenticationToken, 
    // so we can directly cast it to user here 

    return watchlistService.addToWatchlist(user.getId(),movieId);
  }

  @GetMapping("/get")
  public List<WatchlistResponse> getUserWatchlist(){

    Authentication auth=SecurityContextHolder.getContext().getAuthentication();
    User user=(User) auth.getPrincipal();

    return watchlistService.getUserWatchlist(user.getId());

  }

  @DeleteMapping("/remove")
  public void removeFromWatchlist(@RequestParam Long movieId){

    Authentication auth=SecurityContextHolder.getContext().getAuthentication();
    User user=(User) auth.getPrincipal();

    watchlistService.removeFromWatchlist(user.getId(),movieId);

  }

}
