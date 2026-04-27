package com.example.cinetrackerbackend.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a video/trailer from TMDB videos API.
 */
@Getter
@AllArgsConstructor
public class TrailerResponse {
    private String id;
    private String name;
    private String key;         // YouTube video key
    private String site;        // e.g. "YouTube"
    private String type;        // e.g. "Trailer", "Teaser"
    private boolean official;
}
