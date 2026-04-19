package com.example.cinetrackerbackend.rating;

public interface RatingSummaryProjection{

   Long getMovieId();
   Double getAverageRating();
   Long getRatingCount();

}
