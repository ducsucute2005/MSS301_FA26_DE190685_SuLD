package com.fudn.bookingservice.service;

import com.fudn.bookingservice.client.MovieClient;
import com.fudn.bookingservice.dto.BookingItemRequest;
import com.fudn.bookingservice.dto.BookingResponse;
import com.fudn.bookingservice.dto.CreateBookingRequest;
import com.fudn.bookingservice.dto.ShowtimeResponse;
import com.fudn.bookingservice.exception.ApiException;
import com.fudn.bookingservice.model.Booking;
import com.fudn.bookingservice.model.BookingDetail;
import com.fudn.bookingservice.model.BookingStatus;
import com.fudn.bookingservice.repository.BookingDetailRepository;
import com.fudn.bookingservice.repository.BookingRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final MovieClient movieClient;

    @Transactional
    public BookingResponse create(Long customerId, CreateBookingRequest request) {
        Map<String, ShowtimeResponse> showtimeCache = new HashMap<>();
        Map<String, Set<String>> bookedSeatCache = new HashMap<>();
        Set<String> requestedSeats = new HashSet<>();

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setBookingDate(LocalDateTime.now());
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        BigDecimal total = BigDecimal.ZERO;

        for (BookingItemRequest item : request.items()) {
            String seat = item.seatCode();
            if (!requestedSeats.add(item.showtimeId() + "#" + seat)) {
                throw ApiException.badRequest(
                        "Duplicate seat " + seat + " of showtime " + item.showtimeId() + " in request");
            }

            ShowtimeResponse showtime = showtimeCache.computeIfAbsent(item.showtimeId(), this::fetchShowtime);
            validateShowtime(showtime);
            validateSeat(seat, showtime);

            Set<String> bookedSeats = bookedSeatCache.computeIfAbsent(showtime.showtimeId(),
                    id -> new HashSet<>(bookingDetailRepository.findSeatCodesByShowtime(id, BookingStatus.CONFIRMED)));
            if (bookedSeats.contains(seat)) {
                throw ApiException.conflict(
                        "Seat " + seat + " of showtime " + showtime.showtimeId() + " is already booked");
            }

            BookingDetail detail = new BookingDetail();
            detail.setShowtimeId(showtime.showtimeId());
            detail.setSeatCode(seat);
            detail.setPrice(showtime.ticketPrice());
            detail.setMovieId(showtime.movieId());
            detail.setMovieTitle(showtime.movieTitle());
            detail.setRoomName(showtime.roomName());
            detail.setShowtimeStart(showtime.startTime());
            booking.addDetail(detail);
            total = total.add(showtime.ticketPrice());
        }

        booking.setTotalPrice(total);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} created for customer {} with {} ticket(s), total {}",
                saved.getBookingId(), customerId, saved.getDetails().size(), total);
        return BookingResponse.from(saved);
    }

    private ShowtimeResponse fetchShowtime(String showtimeId) {
        try {
            return movieClient.getShowtime(showtimeId);
        } catch (FeignException.NotFound exception) {
            throw ApiException.notFound("Showtime not found with id: " + showtimeId);
        } catch (FeignException exception) {
            log.error("Cannot call movie-service: {}", exception.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Movie service is unavailable. Please try again later.");
        }
    }

    private void validateShowtime(ShowtimeResponse showtime) {
        if (!"SCHEDULED".equals(showtime.showtimeStatus())) {
            throw ApiException.badRequest("Showtime " + showtime.showtimeId()
                    + " is not available (" + showtime.showtimeStatus() + ")");
        }
        if (!showtime.startTime().isAfter(LocalDateTime.now())) {
            throw ApiException.badRequest("Showtime " + showtime.showtimeId() + " has already started");
        }
    }

    private void validateSeat(String seat, ShowtimeResponse showtime) {
        if (seat == null || !seat.matches("^[A-Z][1-9][0-9]?$")) {
            throw ApiException.badRequest("Invalid seat code: " + seat);
        }

        int rowIndex = seat.charAt(0) - 'A';
        int number = Integer.parseInt(seat.substring(1));
        if (rowIndex >= showtime.seatRows() || number > showtime.seatsPerRow()) {
            char lastRow = (char) ('A' + showtime.seatRows() - 1);
            throw ApiException.badRequest("Seat " + seat + " does not exist in room " + showtime.roomName()
                    + " (rows A-" + lastRow + ", seats 1-" + showtime.seatsPerRow() + ")");
        }
    }
}
