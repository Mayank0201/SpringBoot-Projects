package com.example.cinetrackerbackend.watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface WatchlistRepository extends JpaRepository<Watchlist,Long>{

    List<Watchlist> findByUser_Id(Long userId);
    void deleteByUser_IdAndMovie_Id(Long userId,Long movieId);
    boolean existsByUser_IdAndMovie_Id(Long userId,Long movieId);
}
