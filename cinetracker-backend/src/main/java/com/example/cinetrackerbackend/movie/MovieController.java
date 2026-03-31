package com.example.cinetrackerbackend.movie;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.cinetrackerbackend.movie.dto.MovieSearchResponse;
import com.example.cinetrackerbackend.movie.dto.HomeScreenMovieResponse;
import com.example.cinetrackerbackend.movie.dto.GenreResponse;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController{

  private final MovieService movieService;


  @GetMapping("/search")
  public List<MovieSearchResponse> searchMovies(@RequestParam String query){
    return movieService.searchMovies(query);
  }

  @GetMapping("/popular")
  public List<HomeScreenMovieResponse> getPopularMovies(){
    return movieService.getPopularMovies();
  }

  @GetMapping("/genres")
  public List<GenreResponse> getGenres(){
    return movieService.getGenres();
  }

  @GetMapping("/by-genre")
  public List<HomeScreenMovieResponse> getMoviesByGenre(@RequestParam Long genreId){
    return movieService.getMoviesByGenre(genreId);
  }
}
