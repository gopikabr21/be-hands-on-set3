package com.roombooking.controller;

import com.roombooking.dto.BookingResponse;
import com.roombooking.dto.CreateBookingRequest;
import com.roombooking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookings;

    public BookingController(BookingService bookings) {
        this.bookings = bookings;
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@RequestBody CreateBookingRequest request) {
        return bookings.create(request);
    }

    @GetMapping("/bookings/{id}")
    public BookingResponse get(@PathVariable Long id) {
        return bookings.get(id);
    }

    @GetMapping("/rooms/{roomName}/bookings")
    public List<BookingResponse> listForDate(@PathVariable String roomName, @RequestParam LocalDate date) {
        return bookings.listForDate(roomName, date);
    }

    @DeleteMapping("/bookings/{id}")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookings.cancel(id);
    }
}
