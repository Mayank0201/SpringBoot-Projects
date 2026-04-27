package com.example.cinetrackerbackend.watchlist;

import com.example.cinetrackerbackend.common.ApiResponse;
import com.example.cinetrackerbackend.movie.dto.PaginatedResponse;
import com.example.cinetrackerbackend.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
  public ResponseEntity<ApiResponse<WatchlistResponse>> addToWatchlist(@Valid @RequestBody WatchlistMovieRequest request){
    WatchlistResponse createdItem = watchlistService.addToWatchlist(getAuthenticatedUserId(), request.getMovieId());
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success("Added to watchlist", HttpStatus.CREATED.value(), createdItem));
  }

  @GetMapping("/get")
  public ResponseEntity<ApiResponse<PaginatedResponse<WatchlistResponse>>> getUserWatchlist(
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "status", required = false) String status){


    WatchlistStatus statusEnum = null;
    if (status != null && !status.isBlank()) {
      try {
        statusEnum = WatchlistStatus.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
          .body(ApiResponse.success("Invalid status. Use PENDING, ACTIVE, or COMPLETED", HttpStatus.BAD_REQUEST.value(), null));
      }
    }
    PaginatedResponse<WatchlistResponse> data = watchlistService.getUserWatchlist(getAuthenticatedUserId(), page, size, statusEnum);
    return ResponseEntity.ok(ApiResponse.success("Watchlist fetched", HttpStatus.OK.value(), data));
  }

  @PatchMapping("/{movieId}/status")
  public ResponseEntity<ApiResponse<WatchlistResponse>> updateStatus(
      @PathVariable Long movieId,
      @Valid @RequestBody WatchlistStatusUpdateRequest request) {
    WatchlistStatus newStatus;
    try {
      newStatus = WatchlistStatus.valueOf(request.getStatus().toUpperCase());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
        .body(ApiResponse.success("Invalid status. Use PENDING, ACTIVE, or COMPLETED", HttpStatus.BAD_REQUEST.value(), null));
    }
    WatchlistResponse updated = watchlistService.updateWatchlistStatus(getAuthenticatedUserId(), movieId, newStatus);
    return ResponseEntity.ok(ApiResponse.success("Status updated", HttpStatus.OK.value(), updated));
  }


  @DeleteMapping("/remove")
  public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(@Valid @RequestBody WatchlistMovieRequest request){
    watchlistService.removeFromWatchlist(getAuthenticatedUserId(), request.getMovieId());
    return ResponseEntity.ok(ApiResponse.success("Removed from watchlist", HttpStatus.OK.value(), null));
  }

  private Long getAuthenticatedUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) auth.getPrincipal();
    return user.getId();
  }
}