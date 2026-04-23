package com.example.cinetrackerbackend.rating;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class RatingSummaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long movieId;
    private Double averageRating;
    private Long ratingCount;
    private Double myRating;
}

