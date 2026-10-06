package com.fudn.movieservice.repository;

import com.fudn.movieservice.model.Showtime;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShowtimeRepository extends MongoRepository<Showtime, String> {

    boolean existsByRoomId(String roomId);

    boolean existsByMovieId(String movieId);
}
