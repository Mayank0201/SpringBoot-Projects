package com.example.cinetrackerbackend.rating;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.Data;
import java.time.LocalDateTime;


import java.io.Serializable;

@Entity
// movie_ratings table stores user scores for specific movies
@Table(name="movie_ratings",
    uniqueConstraints= {
        @UniqueConstraint(columnNames={"movie_id","user_id"})
    },
    indexes = {
        @Index(name = "idx_movie_ratings_movie_id", columnList = "movie_id"),
        @Index(name = "idx_movie_ratings_user_id", columnList = "user_id")
    }
)

@Data
public class MovieRating implements Serializable {
    private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private com.example.cinetrackerbackend.user.User user;

    @com.fasterxml.jackson.annotation.JsonProperty("userId")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("username")
    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }

    @Column(nullable = false)
    private Double rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "helpful_count", nullable = false)
    private Long helpfulCount = 0L;

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
