package com.example.cinetrackerbackend.rating;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class RatingSummaryDTO {
    private Long movieId;
    private Double averageRating;
    private Long ratingCount;
    private Double myRating;
}

