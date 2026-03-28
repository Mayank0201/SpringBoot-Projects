package com.example.cinetrackerbackend.watchlist;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.cinetrackerbackend.movie.MovieRepository;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.movie.Movie;
import com.example.cinetrackerbackend.user.User;

@Service
@RequiredArgsConstructor
public class WatchlistService{

  private final WatchlistRepository watchlistRepo;
  private final MovieRepository movieRepo;
  private final UserRepository userRepo;

  public Watchlist addToWatchlist(Long userId,Long movieId){
    User user=userRepo.findById(userId)
	  .orElseThrow(()->new RuntimeException("User not found"));

    Movie movie=movieRepo.findById(movieId)
	  .orElseThrow(()->new RuntimeException("Movie not found"));

    Watchlist watchlist=new Watchlist(null,user,movie);
    return watchlistRepo.save(watchlist);
  }

}

