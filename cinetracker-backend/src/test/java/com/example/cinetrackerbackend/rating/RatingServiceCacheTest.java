package com.example.cinetrackerbackend.rating;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RatingServiceCacheTest {

    @Test
    void testRatingSummaryDTOCanBeCreated() {
        RatingSummaryDTO dto = new RatingSummaryDTO(1L, 4.5, 100L, 5.0);
        
        assertEquals(1L, dto.getMovieId());
        assertEquals(4.5, dto.getAverageRating());
        assertEquals(100L, dto.getRatingCount());
        assertEquals(5.0, dto.getMyRating());
    }

    @Test
    void testRatingSummaryDTOWithAllArgsConstructor() {
        RatingSummaryDTO dto = new RatingSummaryDTO(2L, 4.5, 100L, 5.0);
        
        assertEquals(2L, dto.getMovieId());
        assertEquals(4.5, dto.getAverageRating());
        assertEquals(100L, dto.getRatingCount());
        assertEquals(5.0, dto.getMyRating());
    }

    @Test
    void testRatingSummaryDTOSettersWork() {
        RatingSummaryDTO dto = new RatingSummaryDTO(1L, 3.5, 50L, 4.0);
        
        dto.setMovieId(2L);
        dto.setAverageRating(3.5);
        dto.setRatingCount(50L);
        dto.setMyRating(4.0);
        
        assertEquals(2L, dto.getMovieId());
        assertEquals(3.5, dto.getAverageRating());
        assertEquals(50L, dto.getRatingCount());
        assertEquals(4.0, dto.getMyRating());
    }

    @Test
    void testRatingSummaryDTOEqualsAndHashCode() {
        RatingSummaryDTO dto1 = new RatingSummaryDTO(1L, 4.5, 100L, 5.0);
        RatingSummaryDTO dto2 = new RatingSummaryDTO(1L, 4.5, 100L, 5.0);
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testRatingSummaryDTONotEquals() {
        RatingSummaryDTO dto1 = new RatingSummaryDTO(1L, 4.5, 100L, 5.0);
        RatingSummaryDTO dto2 = new RatingSummaryDTO(2L, 3.5, 50L, 4.0);
        
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testRatingSummaryDTOToString() {
        RatingSummaryDTO dto = new RatingSummaryDTO(1L, 4.5, 100L, 5.0);
        String str = dto.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("RatingSummaryDTO"));
    }
}
