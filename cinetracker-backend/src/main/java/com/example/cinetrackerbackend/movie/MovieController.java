package com.example.cinetrackerbackend.movie;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController{

  private final MovieService movieService;

  @PostMapping("/add")
  public Movie saveMovie(@RequestBody Movie movie){
    return movieService.saveMovie(movie);
  }

  @GetMapping("/getMovies")
  public List<Movie> getAllMovies(){
    return movieService.getMovies();
  }

}
