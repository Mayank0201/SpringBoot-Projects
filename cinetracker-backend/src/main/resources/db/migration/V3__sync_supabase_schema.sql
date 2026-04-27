-- Master Schema Sync (Based on Supabase Schema Image)

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    refresh_token_hash VARCHAR(255),
    refresh_token_expires_at TIMESTAMP WITH TIME ZONE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    email_verification_token_expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    xp BIGINT NOT NULL DEFAULT 0,
    level INTEGER NOT NULL DEFAULT 1
);

-- 2. Movie Table (Metadata Cache)
CREATE TABLE IF NOT EXISTS movie (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    genre VARCHAR(255),
    release_year INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Watchlist Table
CREATE TABLE IF NOT EXISTS watchlist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    movie_id BIGINT, -- Links to Movie ID (TMDB)
    title VARCHAR(255),
    poster_url VARCHAR(255),
    overview TEXT,
    rating DOUBLE PRECISION,
    release_date VARCHAR(255),
    release_year INTEGER,
    genre VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    UNIQUE(user_id, movie_id)
);

-- 4. Movie Ratings Table
CREATE TABLE IF NOT EXISTS movie_ratings (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    rating DOUBLE PRECISION NOT NULL,
    comment TEXT,
    helpful_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(movie_id, user_id)
);

-- 5. Follows Table
CREATE TABLE IF NOT EXISTS follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    following_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(follower_id, following_id)
);

-- 6. Daily XP Tracker Table
CREATE TABLE IF NOT EXISTS daily_xp_tracker (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL,
    action_date DATE NOT NULL,
    count INTEGER NOT NULL DEFAULT 0,
    UNIQUE(user_id, action_type, action_date)
);

-- 7. Badges Table
CREATE TABLE IF NOT EXISTS badges (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(255),
    type VARCHAR(50)
);

-- 8. User Badges (Junction Table)
CREATE TABLE IF NOT EXISTS user_badges (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    badge_id BIGINT REFERENCES badges(id) ON DELETE CASCADE,
    earned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, badge_id)
);

-- PERFORMANCE INDEXES (WITH SAFETY CHECKS)
CREATE INDEX IF NOT EXISTS idx_watchlist_user_id ON watchlist(user_id);
CREATE INDEX IF NOT EXISTS idx_watchlist_movie_id ON watchlist(movie_id);
CREATE INDEX IF NOT EXISTS idx_watchlist_status ON watchlist(status);

CREATE INDEX IF NOT EXISTS idx_movie_ratings_movie_id ON movie_ratings(movie_id);
CREATE INDEX IF NOT EXISTS idx_movie_ratings_user_id ON movie_ratings(user_id);

CREATE INDEX IF NOT EXISTS idx_follows_follower_id ON follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following_id ON follows(following_id);

CREATE INDEX IF NOT EXISTS idx_user_badges_user_id ON user_badges(user_id);

CREATE INDEX IF NOT EXISTS idx_daily_xp_tracker_user_date ON daily_xp_tracker(user_id, action_date);
