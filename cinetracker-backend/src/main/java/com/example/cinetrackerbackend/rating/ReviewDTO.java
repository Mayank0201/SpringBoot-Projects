package com.example.cinetrackerbackend.rating;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long movieId;
    private Long userId;
    private String username;
    private Double rating;
    private String comment;
    private Long helpfulCount;
    private Boolean isHelpful; // Whether the current user has marked it helpful
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReviewDTO(MovieRating rating, Boolean isHelpful) {
        this.id = rating.getId();
        this.movieId = rating.getMovieId();
        this.userId = rating.getUserId();
        this.username = rating.getUsername();
        this.rating = rating.getRating();
        this.comment = rating.getComment();
        this.helpfulCount = rating.getHelpfulCount();
        this.isHelpful = isHelpful;
        this.createdAt = rating.getCreatedAt();
        this.updatedAt = rating.getUpdatedAt();
    }
}
