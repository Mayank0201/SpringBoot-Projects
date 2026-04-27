package com.example.cinetrackerbackend.watchlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface WatchlistRepository extends JpaRepository<Watchlist,Long>{

    List<Watchlist> findByUser_Id(Long userId);
    Page<Watchlist> findByUser_Id(Long userId, Pageable pageable);
    Page<Watchlist> findByUser_IdAndStatus(Long userId, WatchlistStatus status, Pageable pageable);
    Optional<Watchlist> findByUser_IdAndMovieId(Long userId, Long movieId);
    void deleteByUser_IdAndMovieId(Long userId,Long movieId);
    boolean existsByUser_IdAndMovieId(Long userId,Long movieId);
}
