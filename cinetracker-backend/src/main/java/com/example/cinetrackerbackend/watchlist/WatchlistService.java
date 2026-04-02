package com.example.cinetrackerbackend.watchlist;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.user.User;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import com.example.cinetrackerbackend.exception.ApiException;
import com.example.cinetrackerbackend.movie.TmdbClient;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WatchlistService{

  private final WatchlistRepository watchlistRepo;
  private final UserRepository userRepo;
  private final TmdbClient tmdbClient;

  public Watchlist addToWatchlist(Long userId,Long movieId){
    
    if((watchlistRepo.existsByUser_IdAndMovieId(userId, movieId))){
      throw new ApiException("Movie already in watchlist");
    }
    
    
    User user=userRepo.findById(userId)
	  .orElseThrow(()->new ApiException("User not found"));

    Map<String, Object> movieData = tmdbClient.getMovieDetails(movieId);

    String title = (String) movieData.get("title");

    String posterPath = (String) movieData.get("poster_path");
    String posterUrl = posterPath != null
        ? "https://image.tmdb.org/t/p/w500" + posterPath
        : null;

    String releaseDate = (String) movieData.get("release_date");
    int releaseYear = releaseDate != null && releaseDate.length() >= 4
        ? Integer.parseInt(releaseDate.substring(0, 4))
        : 0;

    Watchlist watchlist = new Watchlist(
        null,
        user,
        movieId,
        title,
        posterUrl,
        releaseYear,
        "N/A"
    );

    return watchlistRepo.save(watchlist);
  }

  public List<WatchlistResponse> getUserWatchlist(Long userId) {

    return watchlistRepo.findByUser_Id(userId)
        .stream()
        .map(w -> new WatchlistResponse(
            w.getId(),
            w.getMovieId(),
            w.getTitle(),
            w.getGenre(),
            w.getReleaseYear()
        ))
        .collect(Collectors.toList());
  }

  @Transactional//entitymanager needs to be open for delete operation, so we need to add transactional annotation here
  //so em knows to flush the changes to the database after the method execution is completed
  public void removeFromWatchlist(Long userId, Long movieId){

    if (!watchlistRepo.existsByUser_IdAndMovieId(userId, movieId)) {
      throw new ApiException("Movie not in watchlist");
    }

    watchlistRepo.deleteByUser_IdAndMovieId(userId, movieId);
  }

}

