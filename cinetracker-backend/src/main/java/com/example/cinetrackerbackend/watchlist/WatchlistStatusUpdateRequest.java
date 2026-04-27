package com.example.cinetrackerbackend.watchlist;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistStatusUpdateRequest {

  @NotBlank(message = "status is required")
  private String status;
}

