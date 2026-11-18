package com.roombooking.service;

import com.roombooking.dto.BookingResponse;
import com.roombooking.dto.CreateBookingRequest;
import com.roombooking.entity.Booking;
import com.roombooking.entity.Room;
import com.roombooking.exception.ApiException;
import com.roombooking.repository.BookingRepository;
import com.roombooking.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private static final LocalTime OPEN = LocalTime.of(9, 0);
    private static final LocalTime CLOSE = LocalTime.of(18, 0);
    private static final Duration MAX_DURATION = Duration.ofHours(4);
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final RoomRepository rooms;
    private final BookingRepository bookings;
    private final Clock clock;

    public BookingService(RoomRepository rooms, BookingRepository bookings, Clock clock) {
        this.rooms = rooms;
        this.bookings = bookings;
        this.clock = clock;
    }

    @Transactional
    public BookingResponse create(CreateBookingRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, Map.of("body", "Request body is required"));
        }

        Map<String, String> errors = new LinkedHashMap<>();
        String roomName = blankToNull(request.room());
        String title = blankToNull(request.title());
        String organiser = blankToNull(request.organiserEmail());

        if (roomName == null) {
            errors.put("room", "Room is required");
        }
        if (title == null) {
            errors.put("title", "Title is required");
        }
        if (organiser == null) {
            errors.put("organiserEmail", "Organiser email is required");
        } else if (!isEmail(organiser)) {
            errors.put("organiserEmail", "Organiser email is invalid");
        }

        List<String> attendees = normaliseAttendees(organiser, request.attendeeEmails(), errors);
        validateTimes(request.start(), request.end(), errors);
        if (!errors.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, errors);
        }

        Room room = rooms.findByName(roomName)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        Map.of("room", "Room '" + roomName + "' was not found")));
        if (!room.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, Map.of("room", "Room is not active"));
        }
        if (attendees.size() > room.getCapacity()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    Map.of("attendeeEmails", "Attendees exceed room capacity of " + room.getCapacity()));
        }

        List<Booking> clashes = bookings.findOverlapping(room, request.start(), request.end());
        if (!clashes.isEmpty()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    Map.of("booking", "Clashes with '" + clashes.get(0).getTitle() + "'"));
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setTitle(title);
        booking.setOrganiserEmail(organiser.toLowerCase(Locale.ROOT));
        booking.setAttendeeEmails(new ArrayList<>(attendees));
        booking.setStartTime(request.start());
        booking.setEndTime(request.end());
        booking.setCreatedAt(LocalDateTime.now(clock));
        booking.setCancelled(false);
        return BookingResponse.from(bookings.save(booking));
    }

    public BookingResponse get(Long id) {
        return BookingResponse.from(findBooking(id));
    }

    public List<BookingResponse> listForDate(String roomName, LocalDate date) {
        Room room = rooms.findByName(roomName)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        Map.of("room", "Room '" + roomName + "' was not found")));
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        return bookings.findActiveForDay(room, from, to).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse cancel(Long id) {
        Booking booking = findBooking(id);
        booking.setCancelled(true);
        return BookingResponse.from(booking);
    }

    private Booking findBooking(Long id) {
        return bookings.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        Map.of("id", "Booking '" + id + "' was not found")));
    }

    private List<String> normaliseAttendees(String organiser, List<String> raw, Map<String, String> errors) {
        if (raw == null || raw.isEmpty()) {
            errors.put("attendeeEmails", "Attendees are required");
            return List.of();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String value : raw) {
            if (value == null || !isEmail(value.trim())) {
                errors.put("attendeeEmails", "Every attendee email must be valid");
                return List.of();
            }
            unique.add(value.trim().toLowerCase(Locale.ROOT));
        }
        if (organiser != null && isEmail(organiser) && !unique.contains(organiser.toLowerCase(Locale.ROOT))) {
            errors.put("attendeeEmails", "Organiser must be an attendee");
        }
        return List.copyOf(unique);
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end, Map<String, String> errors) {
        if (start == null) {
            errors.put("start", "Start is required");
        } else if (!isQuarterHour(start)) {
            errors.put("start", "Start must use 15-minute steps");
        } else if (start.toLocalTime().isBefore(OPEN) || !start.toLocalTime().isBefore(CLOSE)) {
            errors.put("start", "Start must be within working hours 09:00 to 18:00");
        } else if (start.isBefore(LocalDateTime.now(clock))) {
            errors.put("start", "Start must not be in the past");
        }

        if (end == null) {
            errors.put("end", "End is required");
        } else if (!isQuarterHour(end)) {
            errors.put("end", "End must use 15-minute steps");
        } else if (end.toLocalTime().isBefore(OPEN) || end.toLocalTime().isAfter(CLOSE)) {
            errors.put("end", "End must be within working hours 09:00 to 18:00");
        }

        if (start == null || end == null || errors.containsKey("start") || errors.containsKey("end")) {
            return;
        }
        if (!start.isBefore(end)) {
            errors.put("end", "End must be after start");
        } else if (!start.toLocalDate().equals(end.toLocalDate())) {
            errors.put("end", "Booking must start and end on the same day");
        } else if (Duration.between(start, end).compareTo(MAX_DURATION) > 0) {
            errors.put("end", "Booking must last at most 4 hours");
        }
    }

    private static boolean isQuarterHour(LocalDateTime time) {
        return time.getMinute() % 15 == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }

    private static boolean isEmail(String value) {
        return EMAIL.matcher(value.trim()).matches();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
