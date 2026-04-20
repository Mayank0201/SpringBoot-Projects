package com.example.cinetrackerbackend.watchlist;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.cinetrackerbackend.user.UserRepository;
import com.example.cinetrackerbackend.user.User;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import com.example.cinetrackerbackend.exception.ApiException;
import com.example.cinetrackerbackend.movie.TmdbClient;
import java.util.Map;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class WatchlistService{

  private final WatchlistRepository watchlistRepo;
  private final UserRepository userRepo;
  private final TmdbClient tmdbClient;

  public WatchlistResponse addToWatchlist(Long userId,Long movieId){
    
    if((watchlistRepo.existsByUser_IdAndMovieId(userId, movieId))){
      throw new ApiException("Movie already in watchlist", HttpStatus.CONFLICT);
    }
    
    
    User user=userRepo.findById(userId)
	  .orElseThrow(()->new ApiException("User not found", HttpStatus.NOT_FOUND));

    Map<String, Object> movieData = null;
    try {
      movieData = tmdbClient.getMovieDetails(movieId);
    } catch (Exception e) {
      throw new ApiException("Unable to fetch movie details from TMDB: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    if (movieData == null || movieData.isEmpty() || movieData.get("id") == null) {
      throw new ApiException("Movie not found in TMDB database", HttpStatus.NOT_FOUND);
    }

    String title = (String) movieData.get("title");
    if (title == null || title.isBlank()) {
      title = (String) movieData.get("name");
    }
    if (title == null || title.isBlank()) {
      throw new ApiException("Invalid movie data: title is missing", HttpStatus.BAD_REQUEST);
    }
    
    String overview = (String) movieData.getOrDefault("overview", "");
    if (overview == null) {
      overview = "";
    }

    Double rating = 0.0;
    Object voteAvg = movieData.get("vote_average");
    if (voteAvg != null && voteAvg instanceof Number) {
      rating = ((Number) voteAvg).doubleValue();
    }

    String posterPath = (String) movieData.get("poster_path");
    String posterUrl = posterPath != null && !posterPath.isBlank()
        ? "https://image.tmdb.org/t/p/w500" + posterPath
        : null;

    String releaseDate = (String) movieData.get("release_date");
    if (releaseDate == null || releaseDate.isBlank()) {
      releaseDate = null;
    }
    
    int releaseYear = 0;
    if (releaseDate != null && releaseDate.length() >= 4) {
      try {
        releaseYear = Integer.parseInt(releaseDate.substring(0, 4));
      } catch (NumberFormatException e) {
        releaseYear = 0;
      }
    }

    List<Map<String, Object>> genres = (List<Map<String, Object>>) movieData.getOrDefault("genres", Collections.emptyList());
    String genre = "N/A";
    if (genres != null && !genres.isEmpty()) {
      genre = genres.stream()
        .map(genreMap -> (String) genreMap.get("name"))
        .filter(Objects::nonNull)
        .collect(Collectors.joining(", "));
      if (genre.isBlank()) {
        genre = "N/A";
      }
    }

    Watchlist watchlist = new Watchlist(
        null,
        user,
        movieId,
        title,
        posterUrl,
        overview,
        rating,
        releaseDate,
        releaseYear,
        genre
    );

    Watchlist saved = watchlistRepo.save(watchlist);
    return toResponse(saved);
  }

  public List<WatchlistResponse> getUserWatchlist(Long userId) {

    return watchlistRepo.findByUser_Id(userId)
        .stream()
      .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional//entitymanager needs to be open for delete operation, so we need to add transactional annotation here
  //so em knows to flush the changes to the database after the method execution is completed
  public void removeFromWatchlist(Long userId, Long movieId){

    if (!watchlistRepo.existsByUser_IdAndMovieId(userId, movieId)) {
      throw new ApiException("Movie not in watchlist", HttpStatus.NOT_FOUND);
    }

    watchlistRepo.deleteByUser_IdAndMovieId(userId, movieId);
  }

  private WatchlistResponse toResponse(Watchlist watchlist) {
    return new WatchlistResponse(
      watchlist.getId(),
      watchlist.getMovieId(),
      watchlist.getTitle(),
      watchlist.getPosterUrl(),
      watchlist.getOverview(),
      watchlist.getRating(),
      watchlist.getRating(),
      watchlist.getReleaseDate(),
      watchlist.getReleaseYear(),
      watchlist.getGenre()
    );
  }

}

