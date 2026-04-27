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
import com.example.cinetrackerbackend.movie.dto.PaginatedResponse;
import com.example.cinetrackerbackend.rating.RatingService;
import com.example.cinetrackerbackend.rating.RatingSummaryDTO;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class WatchlistService{

  private final WatchlistRepository watchlistRepo;
  private final UserRepository userRepo;
  private final TmdbClient tmdbClient;
  private final RatingService ratingService;
  private final com.example.cinetrackerbackend.movie.MovieService movieService;
  private final com.example.cinetrackerbackend.gamification.GamificationService gamificationService;


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

    // Ensure movie exists in local movie table for foreign key constraint
    movieService.ensureMovieExists(movieId);

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
        genre,
        WatchlistStatus.PENDING
    );

    Watchlist saved = watchlistRepo.save(watchlist);
    
    // Award XP
    gamificationService.awardXpWithLimit(userId, com.example.cinetrackerbackend.gamification.GamificationService.XP_ADD_WATCHLIST, "ADD_WATCHLIST", "Added movie to watchlist: " + title);


    return toResponse(saved);
  }


  public PaginatedResponse<WatchlistResponse> getUserWatchlist(Long userId, int page, int size, WatchlistStatus status) {
    int safePage = Math.max(page, 1);
    int safeSize = Math.min(Math.max(size, 1), 50);

    Page<Watchlist> pageData;
    if (status != null) {
      pageData = watchlistRepo.findByUser_IdAndStatus(userId, status, PageRequest.of(safePage - 1, safeSize));
    } else {
      pageData = watchlistRepo.findByUser_Id(userId, PageRequest.of(safePage - 1, safeSize));
    }
    List<Watchlist> items = pageData.getContent();
    
    if (items.isEmpty()) {
      return new PaginatedResponse<>(safePage, pageData.getTotalPages(), Collections.emptyList());
    }
    
    List<Long> movieIds = items.stream()
        .map(Watchlist::getMovieId)
        .collect(Collectors.toList());
    Map<Long, RatingSummaryDTO> ratings = ratingService.getRatingSummariesForMovies(movieIds);
    
    List<WatchlistResponse> results = items.stream()
      .map(item -> toResponse(item, ratings.getOrDefault(item.getMovieId(), new RatingSummaryDTO(item.getMovieId(), 0.0, 0L, null))))
        .collect(Collectors.toList());

    return new PaginatedResponse<>(pageData.getNumber() + 1, pageData.getTotalPages(), results);
  }

  @Transactional
  public void removeFromWatchlist(Long userId, Long movieId){

    if (!watchlistRepo.existsByUser_IdAndMovieId(userId, movieId)) {
      throw new ApiException("Movie not in watchlist", HttpStatus.NOT_FOUND);
    }

    watchlistRepo.deleteByUser_IdAndMovieId(userId, movieId);
  }

  @Transactional
  public WatchlistResponse updateWatchlistStatus(Long userId, Long movieId, WatchlistStatus newStatus) {
    Watchlist watchlist = watchlistRepo.findByUser_IdAndMovieId(userId, movieId)
        .orElseThrow(() -> new ApiException("Movie not in watchlist", HttpStatus.NOT_FOUND));

    WatchlistStatus oldStatus = watchlist.getStatus();
    watchlist.setStatus(newStatus);
    Watchlist saved = watchlistRepo.save(watchlist);
    
    // Award XP if completed
    if (newStatus == WatchlistStatus.COMPLETED && oldStatus != WatchlistStatus.COMPLETED) {
        gamificationService.awardXpWithLimit(userId, com.example.cinetrackerbackend.gamification.GamificationService.XP_COMPLETE_MOVIE, "COMPLETE", "Completed movie: " + watchlist.getTitle());
    }

    
    return toResponse(saved);

  }


  private WatchlistResponse toResponse(Watchlist watchlist, RatingSummaryDTO ratings) {
    return new WatchlistResponse(
      watchlist.getId(),
      watchlist.getMovieId(),
      watchlist.getTitle(),
      watchlist.getPosterUrl(),
      watchlist.getOverview(),
      ratings.getAverageRating() != 0 ? ratings.getAverageRating() : watchlist.getRating(),
      watchlist.getRating(),
      watchlist.getReleaseDate(),
      watchlist.getReleaseYear(),
      watchlist.getGenre(),
      watchlist.getStatus().name()
    );
  }

  private WatchlistResponse toResponse(Watchlist watchlist) {
    return toResponse(watchlist, new RatingSummaryDTO(watchlist.getMovieId(), 0.0, 0L, null));
  }

}

