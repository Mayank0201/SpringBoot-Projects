package com.example.cinetrackerbackend.movie;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController{

  private final MovieService movieService;


  @GetMapping("/search")
  public Map<String ,Object> searchMovies(@RequestParam String query){
    return movieService.searchMovies(query);
  }

}
