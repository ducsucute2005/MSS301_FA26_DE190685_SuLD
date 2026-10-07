package com.fudn.movieservice.dto;

import com.fudn.movieservice.model.CinemaRoom;
import com.fudn.movieservice.model.Movie;
import com.fudn.movieservice.model.Showtime;
import com.fudn.movieservice.model.ShowtimeStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowtimeResponse(String showtimeId, String movieId, String movieTitle,
                               String roomId, String roomName, int seatRows, int seatsPerRow,
                               LocalDateTime startTime, LocalDateTime endTime,
                               BigDecimal ticketPrice, ShowtimeStatus showtimeStatus) {

    public static ShowtimeResponse from(Showtime showtime, Movie movie, CinemaRoom room) {
        return new ShowtimeResponse(showtime.getShowtimeId(),
                movie.getMovieId(), movie.getTitle(),
                room.getRoomId(), room.getRoomName(), room.getSeatRows(), room.getSeatsPerRow(),
                showtime.getStartTime(), showtime.getEndTime(), showtime.getTicketPrice(),
                showtime.getShowtimeStatus());
    }
}
