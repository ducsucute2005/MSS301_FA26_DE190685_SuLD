package com.fudn.movieservice.service;

import com.fudn.movieservice.dto.ShowtimeRequest;
import com.fudn.movieservice.dto.ShowtimeResponse;
import com.fudn.movieservice.exception.ApiException;
import com.fudn.movieservice.model.CinemaRoom;
import com.fudn.movieservice.model.Movie;
import com.fudn.movieservice.model.MovieStatus;
import com.fudn.movieservice.model.RoomStatus;
import com.fudn.movieservice.model.Showtime;
import com.fudn.movieservice.model.ShowtimeStatus;
import com.fudn.movieservice.repository.MovieRepository;
import com.fudn.movieservice.repository.RoomRepository;
import com.fudn.movieservice.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private static final String NO_EXCLUDE = "";

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final MovieService movieService;
    private final RoomService roomService;

    public List<ShowtimeResponse> search(String movieId, LocalDate date) {
        List<Showtime> showtimes = (movieId == null || movieId.isBlank())
                ? showtimeRepository.findAllByOrderByStartTimeAsc()
                : showtimeRepository.findByMovieIdOrderByStartTimeAsc(movieId);
        List<Showtime> filtered = showtimes.stream()
                .filter(showtime -> date == null || showtime.getStartTime().toLocalDate().equals(date))
                .toList();
        return toResponses(filtered);
    }

    public ShowtimeResponse create(ShowtimeRequest request) {
        Showtime showtime = new Showtime();
        showtime.setShowtimeStatus(ShowtimeStatus.SCHEDULED);
        return apply(showtime, request, NO_EXCLUDE);
    }

    public ShowtimeResponse update(String id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Showtime not found with id: " + id));
        if (showtime.getShowtimeStatus() == ShowtimeStatus.CANCELLED) {
            throw ApiException.badRequest("Cannot update a cancelled showtime");
        }
        return apply(showtime, request, id);
    }

    public void cancel(String id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Showtime not found with id: " + id));
        showtime.setShowtimeStatus(ShowtimeStatus.CANCELLED);
        showtimeRepository.save(showtime);
    }

    private List<ShowtimeResponse> toResponses(List<Showtime> showtimes) {
        if (showtimes.isEmpty()) {
            return List.of();
        }

        Map<String, Movie> movies = movieRepository
                .findAllById(showtimes.stream().map(Showtime::getMovieId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Movie::getMovieId, Function.identity()));
        Map<String, CinemaRoom> rooms = roomRepository
                .findAllById(showtimes.stream().map(Showtime::getRoomId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(CinemaRoom::getRoomId, Function.identity()));

        return showtimes.stream()
                .map(showtime -> {
                    Movie movie = movies.get(showtime.getMovieId());
                    CinemaRoom room = rooms.get(showtime.getRoomId());
                    if (movie == null || room == null) {
                        throw new IllegalStateException(
                                "Showtime " + showtime.getShowtimeId() + " references a missing movie or room");
                    }
                    return ShowtimeResponse.from(showtime, movie, room);
                })
                .toList();
    }

    private ShowtimeResponse apply(Showtime showtime, ShowtimeRequest request, String excludeId) {
        Movie movie = movieService.find(request.movieId());
        CinemaRoom room = roomService.find(request.roomId());

        if (movie.getMovieStatus() == MovieStatus.ENDED) {
            throw ApiException.badRequest("Movie '" + movie.getTitle() + "' has ENDED and cannot be scheduled");
        }
        if (room.getRoomStatus() != RoomStatus.ACTIVE) {
            throw ApiException.badRequest("Room '" + room.getRoomName() + "' is not ACTIVE");
        }
        if (!request.startTime().isAfter(LocalDateTime.now())) {
            throw ApiException.badRequest("Start time must be in the future");
        }

        LocalDateTime endTime = request.startTime().plusMinutes(movie.getDurationMinutes());
        long overlaps = showtimeRepository
                .countByRoomIdAndShowtimeStatusAndStartTimeLessThanAndEndTimeGreaterThanAndShowtimeIdNot(
                        room.getRoomId(), ShowtimeStatus.SCHEDULED, endTime, request.startTime(), excludeId);
        if (overlaps > 0) {
            throw ApiException.conflict("Room '" + room.getRoomName() + "' already has a showtime between "
                    + request.startTime() + " and " + endTime);
        }

        showtime.setMovieId(movie.getMovieId());
        showtime.setRoomId(room.getRoomId());
        showtime.setStartTime(request.startTime());
        showtime.setEndTime(endTime);
        showtime.setTicketPrice(request.ticketPrice());
        Showtime saved = showtimeRepository.save(showtime);
        return ShowtimeResponse.from(saved, movie, room);
    }
}
