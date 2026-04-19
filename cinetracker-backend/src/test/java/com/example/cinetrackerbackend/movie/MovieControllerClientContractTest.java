package com.example.cinetrackerbackend.movie;

import com.example.cinetrackerbackend.movie.dto.HomeScreenMovieResponse;
import com.example.cinetrackerbackend.movie.dto.MovieDetailsResponse;
import com.example.cinetrackerbackend.movie.dto.MovieSearchResponse;
import com.example.cinetrackerbackend.movie.dto.PaginatedResponse;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MovieControllerClientContractTest {

  private MovieService movieService;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    movieService = mock(MovieService.class);
    MovieController controller = new MovieController(movieService);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void searchEndpointShouldRemainUnchangedForClientCalls() throws Exception {
    PaginatedResponse<MovieSearchResponse> payload = new PaginatedResponse<>(
      1,
      1,
      List.of(new MovieSearchResponse(
        1L,
        "Batman",
        "https://image.tmdb.org/t/p/w500/test.jpg",
        "Test overview",
        7.5,
        7.5,
        "2022-03-01",
        2022,
        "Action",
        Collections.singletonList("Action"),
        4.5,
        100L
      ))
    );

    when(movieService.searchMovies("batman", 1)).thenReturn(payload);

    mockMvc.perform(get("/movie/search")
        .param("query", "batman")
        .param("page", "1")
        .header("User-Agent", "curl/8.0")
        .accept("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.results").isArray())
      .andExpect(jsonPath("$.results[0].title").value("Batman"));

    verify(movieService).searchMovies("batman", 1);
  }

  @Test
  void popularAndByGenreShouldStillWorkForFlutterStyleClient() throws Exception {
    PaginatedResponse<HomeScreenMovieResponse> payload = new PaginatedResponse<>(
      1,
      1,
      List.of(new HomeScreenMovieResponse(
        1L,
        "Batman",
        "Batman",
        "https://image.tmdb.org/t/p/w500/test.jpg",
        "Test overview",
        7.8,
        7.8,
        "2022-03-01",
        2022,
        "Action",
        Collections.singletonList("Action"),
        4.5,
        100L
      ))
    );

    when(movieService.getPopularMovies(1)).thenReturn(payload);
    when(movieService.getMoviesByGenre(28L, 1)).thenReturn(payload);

    mockMvc.perform(get("/movie/popular")
        .param("page", "1")
        .header("User-Agent", "Dart/3.0 (dart:io)")
        .accept("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.results").isArray());

    mockMvc.perform(get("/movie/by-genre")
        .param("genreId", "28")
        .param("page", "1")
        .header("User-Agent", "Dart/3.0 (dart:io)")
        .accept("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.results").isArray());

    verify(movieService).getPopularMovies(1);
    verify(movieService).getMoviesByGenre(28L, 1);
  }

  @Test
  void detailsEndpointShouldReturnExpectedFields() throws Exception {
    MovieDetailsResponse details = new MovieDetailsResponse(
      550L,
      550L,
      550L,
      "Fight Club",
      "Fight Club",
      "https://image.tmdb.org/t/p/w500/abc.jpg",
      "/abc.jpg",
      "Mischief. Mayhem. Soap.",
      8.4,
      8.4,
      "1999-10-15",
      "1999-10-15",
      1999,
      "Drama",
      Collections.singletonList("Drama"),
      4.6,
      210L
    );

    when(movieService.getMovieDetails(550L)).thenReturn(details);

    mockMvc.perform(get("/movie/details")
        .param("movieId", "550")
        .header("User-Agent", "PostmanRuntime/7.0")
        .accept("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.movieId").value(550))
      .andExpect(jsonPath("$.title").value("Fight Club"))
      .andExpect(jsonPath("$.overview").value("Mischief. Mayhem. Soap."));

    verify(movieService).getMovieDetails(550L);
  }
}