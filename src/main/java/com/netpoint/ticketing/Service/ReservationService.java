package com.netpoint.ticketing.Service;

import com.netpoint.ticketing.DTO.ReservationRequestDTO;
import com.netpoint.ticketing.DTO.ReservationResponseDTO;
import com.netpoint.ticketing.Enum.ReservationStatus;
import com.netpoint.ticketing.Enum.SeatStatus;
import com.netpoint.ticketing.Exceptions.ReservationException;
import com.netpoint.ticketing.Model.*;
import com.netpoint.ticketing.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private static final int HOLD_MINUTES = 10;

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationItemRepository reservationItemRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;


    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO dto) {

        // 1. Identify current customer from JWT
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ReservationException("Authenticated user not found"));

        // 2. Load event
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ReservationException("Event not found"));

        // 3. *** LOCK THE SEAT ROWS ***
        List<Seat> seats = seatRepository.findAllByIdInAndEventIdForUpdate(
                dto.getSeatIds(), event.getId());

        if (seats.size() != dto.getSeatIds().size()) {
            throw new ReservationException("One or more seats do not belong to this event");
        }

        // 4. Every seat must currently be AVAILABLE
        //    (we are holding the lock, so no other transaction can flip it under us)
        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new ReservationException("Seat " + seat.getSeatNumber() + " is not available");
            }
        }

        // 5. Build the reservation
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = Reservation.builder()
                .customer(customer)
                .event(event)
                .status(ReservationStatus.PENDING)
                .createdAt(now)
                .expiresAt(now.plusMinutes(HOLD_MINUTES))
                .build();

        reservation = reservationRepository.save(reservation);

        // 6. Create items + flip seats to HELD
        List<ReservationItem> items = new ArrayList<>();
        for (Seat seat : seats) {
            items.add(ReservationItem.builder()
                    .reservation(reservation)
                    .seat(seat)
                    .priceAtReservation(event.getTicketPrice())
                    .build());
            seat.setStatus(SeatStatus.HELD);
        }
        reservationItemRepository.saveAll(items);
        seatRepository.saveAll(seats);

        return toResponse(reservation, items, seats);
    }

    public ReservationResponseDTO getById(Long id) {
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationException("Reservation not found"));
        return toResponseFromDb(r);
    }

    public List<ReservationResponseDTO> myReservations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ReservationException("User not found"));
        return reservationRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream()
                .map(this::toResponseFromDb)
                .toList();
    }

    // Mapping helpers

    private ReservationResponseDTO toResponse(Reservation r, List<ReservationItem> items, List<Seat> seats) {
        ReservationResponseDTO dto = base(r);
        List<ReservationResponseDTO.ReservedSeatDTO> seatDtos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < items.size(); i++) {
            ReservationItem item = items.get(i);
            Seat seat = seats.get(i);
            ReservationResponseDTO.ReservedSeatDTO s = new ReservationResponseDTO.ReservedSeatDTO();
            s.setSeatId(seat.getId());
            s.setSeatNumber(seat.getSeatNumber());
            s.setPrice(item.getPriceAtReservation());
            seatDtos.add(s);
            total = total.add(item.getPriceAtReservation());
        }
        dto.setSeats(seatDtos);
        dto.setTotalPrice(total);
        return dto;
    }

    private ReservationResponseDTO toResponseFromDb(Reservation r) {
        ReservationResponseDTO dto = base(r);
        List<ReservationItem> items = reservationItemRepository.findByReservationId(r.getId());
        List<ReservationResponseDTO.ReservedSeatDTO> seatDtos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ReservationItem item : items) {
            ReservationResponseDTO.ReservedSeatDTO s = new ReservationResponseDTO.ReservedSeatDTO();
            s.setSeatId(item.getSeat().getId());
            s.setSeatNumber(item.getSeat().getSeatNumber());
            s.setPrice(item.getPriceAtReservation());
            seatDtos.add(s);
            total = total.add(item.getPriceAtReservation());
        }
        dto.setSeats(seatDtos);
        dto.setTotalPrice(total);
        return dto;
    }

    private ReservationResponseDTO base(Reservation r) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(r.getId());
        dto.setCustomerId(r.getCustomer().getId());
        dto.setCustomerName(r.getCustomer().getName());
        dto.setEventId(r.getEvent().getId());
        dto.setEventTitle(r.getEvent().getTitle());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setExpiresAt(r.getExpiresAt());
        return dto;
    }
}