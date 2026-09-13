package com.netpoint.ticketing.Repository;

import com.netpoint.ticketing.Enum.SeatStatus;
import com.netpoint.ticketing.Model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventId(Long eventId);
    List<Seat> findByEventIdAndStatus(Long eventId, SeatStatus status);
    long countByEventIdAndStatus(Long eventId, SeatStatus status);
}