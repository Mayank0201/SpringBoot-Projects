package com.example.cinetrackerbackend.watchlist;

import com.example.cinetrackerbackend.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/watchlist")
public class WatchlistController {

  private final WatchlistService watchlistService;

  @PostMapping("/add")
  public ResponseEntity<WatchlistResponse> addToWatchlist(@Valid @RequestBody WatchlistMovieRequest request){
    WatchlistResponse createdItem = watchlistService.addToWatchlist(getAuthenticatedUserId(), request.getMovieId());
    return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
  }

  @GetMapping("/get")
  public List<WatchlistResponse> getUserWatchlist(){
    return watchlistService.getUserWatchlist(getAuthenticatedUserId());
  }

  @DeleteMapping("/remove")
  public ResponseEntity<Map<String, String>> removeFromWatchlist(@Valid @RequestBody WatchlistMovieRequest request){
    watchlistService.removeFromWatchlist(getAuthenticatedUserId(), request.getMovieId());
    return ResponseEntity.ok(Map.of("message", "Removed from watchlist"));
  }

  private Long getAuthenticatedUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) auth.getPrincipal();
    return user.getId();
  }
}