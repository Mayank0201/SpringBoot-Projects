create index idx_watchlist_user_id on watchlist(user_id);
create index idx_watchlist_movie_id on watchlist(movie_id);
create index idx_user_id_email on "user"(email);
CREATE INDEX IF NOT EXISTS idx_watchlist_user_movie ON watchlist(user_id, movie_id);
CREATE INDEX IF NOT EXISTS idx_movie_ratings_user_movie ON movie_ratings(user_id, movie_id);
CREATE INDEX IF NOT EXISTS idx_movie_ratings_movie ON movie_ratings(movie_id);
CREATE INDEX IF NOT EXISTS idx_user_email ON "user"(email);
