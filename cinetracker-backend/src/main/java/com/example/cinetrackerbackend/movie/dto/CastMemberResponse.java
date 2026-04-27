package com.example.cinetrackerbackend.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a cast member from TMDB credits.
 */
@Getter
@AllArgsConstructor
public class CastMemberResponse {
    private Long id;
    private String name;
    private String character;
    private String profileUrl;
    private int order;
}
