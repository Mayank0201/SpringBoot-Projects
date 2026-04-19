create table movie_ratings(

    id bigserial primary key,
    movie_id bigint not null,
    user_id bigint not null,
    rating numeric(2,1) not null check (rating >= 1.0 and rating <=5.0),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    unique(movie_id, user_id)
);

create index idx_movie_id on movie_ratings(movie_id);
create index idx_user_id on movie_ratings(user_id);
