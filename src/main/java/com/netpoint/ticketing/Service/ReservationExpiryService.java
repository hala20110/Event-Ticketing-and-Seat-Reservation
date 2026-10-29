package com.netpoint.ticketing.Service;

import com.netpoint.ticketing.Enum.ReservationStatus;
import com.netpoint.ticketing.Enum.SeatStatus;
import com.netpoint.ticketing.Model.Reservation;
import com.netpoint.ticketing.Model.ReservationItem;
import com.netpoint.ticketing.Model.Seat;
import com.netpoint.ticketing.Repository.ReservationItemRepository;
import com.netpoint.ticketing.Repository.ReservationRepository;
import com.netpoint.ticketing.Repository.SeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ReservationExpiryService {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationItemRepository reservationItemRepository;
    @Autowired private SeatRepository seatRepository;

    @Scheduled(fixedRateString = "${reservation.expiry-job-rate}")
    @Transactional
    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();

        //  Find all PENDING reservations that are past their expiry time
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.PENDING, now);

        if (expiredReservations.isEmpty()) {
            return;
        }

        log.info("Found {} expired reservations. Releasing seats...", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            // Change reservation status to EXPIRED
            reservation.setStatus(ReservationStatus.EXPIRED);

            //  Find all seats tied to this reservation and free them
            List<ReservationItem> items = reservationItemRepository.findByReservationId(reservation.getId());
            for (ReservationItem item : items) {
                Seat seat = item.getSeat();
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }

            //  Save the updated reservation
            reservationRepository.save(reservation);
        }
    }
}