package com.netpoint.ticketing.Repository;

import com.netpoint.ticketing.Enum.ReservationStatus;
import com.netpoint.ticketing.Model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime cutoff);
}