package com.example.cinetrackerbackend.movie;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Transient;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie{

  @Id
  private Long id;

  private String title;

  private String genre;

  private int releaseYear;
  
  @jakarta.persistence.Column(name = "created_at", nullable = false, updatable = false)
  private java.time.LocalDateTime createdAt;

  @jakarta.persistence.PrePersist
  protected void onCreate() {
      createdAt = java.time.LocalDateTime.now();
  }


  @Transient
  private Double averageRating;

  @Transient
  private Long ratingCount;

}
