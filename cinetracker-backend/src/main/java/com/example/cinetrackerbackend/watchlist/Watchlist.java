package com.example.cinetrackerbackend.watchlist;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.example.cinetrackerbackend.movie.Movie;
import com.example.cinetrackerbackend.user.User;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"})
)
public class Watchlist{

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private User user; //one user can put multiple watchlist entries

  private Long movieId;
  private String title;
  private String posterUrl;
  private int releaseYear;
  private String genre;

}