package com.example.cinetrackerbackend.movie;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.cinetrackerbackend.movie.dto.MovieSearchResponse;
import com.example.cinetrackerbackend.movie.dto.PaginatedResponse;
import com.example.cinetrackerbackend.movie.dto.HomeScreenMovieResponse;
import com.example.cinetrackerbackend.movie.dto.GenreResponse;
import com.example.cinetrackerbackend.movie.dto.MovieDetailsResponse;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController{

  private final MovieService movieService;


  @GetMapping("/search")
  public PaginatedResponse<MovieSearchResponse> searchMovies(@RequestParam String query, @RequestParam(defaultValue = "1") int page){
    return movieService.searchMovies(query,page);
  }

  @GetMapping("/popular")
  public PaginatedResponse<HomeScreenMovieResponse> getPopularMovies(@RequestParam(defaultValue = "1") int page){
    return movieService.getPopularMovies(page);
  }

  @GetMapping("/genres")
  public List<GenreResponse> getGenres(){
    return movieService.getGenres();
  }

  @GetMapping("/by-genre")
  public PaginatedResponse<HomeScreenMovieResponse> getMoviesByGenre(@RequestParam Long genreId,@RequestParam(defaultValue = "1") int page){
    return movieService.getMoviesByGenre(genreId,page);
  }

  @GetMapping("/details")
  public MovieDetailsResponse getMovieDetails(@RequestParam Long movieId) {
    return movieService.getMovieDetails(movieId);
  }
}
