package com.example.cinetrackerbackend.rating;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRatingRepository extends JpaRepository<MovieRating, Long>{

   Optional<MovieRating> findByMovieIdAndUserId(Long movieId, Long userId);

   @Query(value = "select " + "round(avg(cast(rating as decimal)),1) as averageRating, " +
		  "count(*) as ratingCount " + "from movie_ratings " +
		  "where movie_id = :movieId", nativeQuery = true)
   RatingSummaryProjection getRatingSummary(@Param("movieId") Long movieId);

   @Query(value = "select movie_id, round(avg(cast(rating as decimal)),1) as averageRating, count(*) as ratingCount " +
          "from movie_ratings where movie_id IN :movieIds group by movie_id", nativeQuery = true)
   List<RatingSummaryProjection> getRatingSummariesForMovies(@Param("movieIds") List<Long> movieIds);

   void deleteByMovieIdAndUserId(Long movieId,Long userId);

}
