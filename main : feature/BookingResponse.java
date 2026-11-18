package com.roombooking.dto;

import com.roombooking.entity.Booking;

import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        String room,
        String title,
        String organiserEmail,
        List<String> attendeeEmails,
        LocalDateTime start,
        LocalDateTime end,
        LocalDateTime createdAt,
        boolean cancelled
) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRoom().getName(),
                booking.getTitle(),
                booking.getOrganiserEmail(),
                List.copyOf(booking.getAttendeeEmails()),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getCreatedAt(),
                booking.isCancelled()
        );
    }
}
