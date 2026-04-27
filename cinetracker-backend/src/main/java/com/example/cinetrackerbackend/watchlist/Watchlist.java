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
import jakarta.persistence.Column;
import jakarta.persistence.Index;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import com.example.cinetrackerbackend.user.User;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
// watchlist table tracks movies users want to watch later
@Table(
    name = "watchlist",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}),
    indexes = {
        @Index(name = "idx_watchlist_user_id", columnList = "user_id"),
        @Index(name = "idx_watchlist_movie_id", columnList = "movie_id"),
        @Index(name = "idx_watchlist_status", columnList = "status")
    }
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
  //some movies have overview longer than 255 characters, so we need to use TEXT type in database
  @Column(columnDefinition = "TEXT")
  private String overview;
  private Double rating;
  private String releaseDate;
  private int releaseYear;
  private String genre;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WatchlistStatus status = WatchlistStatus.PENDING;

}