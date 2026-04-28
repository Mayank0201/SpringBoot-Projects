package com.example.cinetrackerbackend.rating;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "movie_rating_helpful",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rating_id", "user_id"})
    }
)
@Data
@NoArgsConstructor
public class MovieRatingHelpful implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating_id", nullable = false)
    private Long ratingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public MovieRatingHelpful(Long ratingId, Long userId) {
        this.ratingId = ratingId;
        this.userId = userId;
    }
}
