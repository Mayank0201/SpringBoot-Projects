package com.example.cinetrackerbackend.rating;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
// movie_ratings table stores user scores for specific movies
@Table(name="movie_ratings",
    uniqueConstraints= {
        @UniqueConstraint(columnNames={"movie_id","user_id"})
    },
    indexes = {
        @Index(name = "idx_movie_ratings_movie_id", columnList = "movie_id")
    }
)

@Data
public class MovieRating{

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double rating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}
