package com.example.cinetrackerbackend.movie;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class MovieService{

  private final MovieRepository movieRepo;

  public Movie saveMovie(Movie movie){
    return movieRepo.save(movie);
  }

  public List<Movie> getMovies(){
    return movieRepo.findAll();
  }

}
