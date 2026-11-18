package com.roombooking.repository;

import com.roombooking.entity.Booking;
import com.roombooking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select b from Booking b
            where b.room = :room
              and b.cancelled = false
              and b.startTime < :end
              and b.endTime > :start
            order by b.startTime asc, b.id asc
            """)
    List<Booking> findOverlapping(
            @Param("room") Room room,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            select b from Booking b
            where b.room = :room
              and b.cancelled = false
              and b.startTime >= :from
              and b.startTime < :to
            order by b.startTime asc, b.id asc
            """)
    List<Booking> findActiveForDay(
            @Param("room") Room room,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
