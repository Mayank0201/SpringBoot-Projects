package com.example.cinetrackerbackend.watchlist;

/**
 * Tracks the lifecycle of a movie in a user's watchlist.
 * PENDING  — added but not started
 * ACTIVE   — currently watching (in progress)
 * COMPLETED — finished watching
 */
public enum WatchlistStatus {
    PENDING,
    ACTIVE,
    COMPLETED
}
