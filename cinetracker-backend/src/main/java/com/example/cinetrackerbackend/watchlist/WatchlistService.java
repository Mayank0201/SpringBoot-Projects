package com.example.cinetrackerbackend.watchlist;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.cinetrackerbackend.movie.MovieRepository;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.movie.Movie;
import com.example.cinetrackerbackend.user.User;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import com.example.cinetrackerbackend.exception.ApiException;

@Service
@RequiredArgsConstructor
public class WatchlistService{

  private final WatchlistRepository watchlistRepo;
  private final MovieRepository movieRepo;
  private final UserRepository userRepo;

  public Watchlist addToWatchlist(Long userId,Long movieId){
    
    if((watchlistRepo.existsByUser_IdAndMovie_Id(userId, movieId))){
      throw new ApiException("Movie already in watchlist");
    }
    
    
    User user=userRepo.findById(userId)
	  .orElseThrow(()->new ApiException("User not found"));

    Movie movie=movieRepo.findById(movieId)
	  .orElseThrow(()->new ApiException("Movie not found"));

    Watchlist watchlist=new Watchlist(null,user,movie);
    return watchlistRepo.save(watchlist);
  }

  public List<WatchlistResponse> getUserWatchlist(Long userId){
    return watchlistRepo.findByUser_Id(userId)
    .stream()
    .map(w->new WatchlistResponse(
      w.getId(),
      w.getMovie().getId(),
      w.getMovie().getTitle(),
      w.getMovie().getGenre(),
      w.getMovie().getReleaseYear()
    )).collect(Collectors.toList());
    
  }

  @Transactional//entitymanager needs to be open for delete operation, so we need to add transactional annotation here
  //so em knows to flush the changes to the database after the method execution is completed
  public void removeFromWatchlist(Long userId,Long movieId){
    watchlistRepo.deleteByUser_IdAndMovie_Id(userId, movieId);
  }

}

