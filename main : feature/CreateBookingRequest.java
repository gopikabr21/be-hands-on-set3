package com.roombooking.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CreateBookingRequest(
        String room,
        String title,
        String organiserEmail,
        List<String> attendeeEmails,
        LocalDateTime start,
        LocalDateTime end
) {
}
