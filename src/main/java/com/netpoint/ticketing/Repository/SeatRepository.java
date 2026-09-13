package com.netpoint.ticketing.Repository;

import com.netpoint.ticketing.Enum.SeatStatus;
import com.netpoint.ticketing.Model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventId(Long eventId);
    List<Seat> findByEventIdAndStatus(Long eventId, SeatStatus status);
    long countByEventIdAndStatus(Long eventId, SeatStatus status);

    @Query("SELECT s FROM Seat s WHERE s.id IN :ids AND s.event.id = :eventId")
    List<Seat> findAllByIdInAndEventId(@Param("ids") List<Long> ids, @Param("eventId") Long eventId);
}