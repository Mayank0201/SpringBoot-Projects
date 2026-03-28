package com.example.cinetrackerbackend.movie;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie{

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;

  private String title;

  private String genre;

  private int releaseYear;

}
