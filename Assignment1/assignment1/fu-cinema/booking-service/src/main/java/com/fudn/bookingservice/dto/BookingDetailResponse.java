package com.fudn.bookingservice.dto;

import com.fudn.bookingservice.model.BookingDetail;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingDetailResponse(String showtimeId, String movieId, String movieTitle, String roomName,
                                    LocalDateTime showtimeStart, String seatCode, BigDecimal price) {
    public static BookingDetailResponse from(BookingDetail detail) {
        return new BookingDetailResponse(detail.getShowtimeId(), detail.getMovieId(), detail.getMovieTitle(),
                detail.getRoomName(), detail.getShowtimeStart(), detail.getSeatCode(), detail.getPrice());
    }
}
