package com.netpoint.ticketing.Service;

import com.netpoint.ticketing.DTO.PaymentRequestDTO;
import com.netpoint.ticketing.DTO.PaymentResponseDTO;
import com.netpoint.ticketing.Enum.PaymentStatus;
import com.netpoint.ticketing.Enum.ReservationStatus;
import com.netpoint.ticketing.Enum.SeatStatus;
import com.netpoint.ticketing.Exceptions.PaymentException;
import com.netpoint.ticketing.Exceptions.ReservationException;
import com.netpoint.ticketing.Model.Payment;
import com.netpoint.ticketing.Model.Reservation;
import com.netpoint.ticketing.Model.ReservationItem;
import com.netpoint.ticketing.Model.Seat;
import com.netpoint.ticketing.Model.User;
import com.netpoint.ticketing.Repository.PaymentRepository;
import com.netpoint.ticketing.Repository.ReservationItemRepository;
import com.netpoint.ticketing.Repository.ReservationRepository;
import com.netpoint.ticketing.Repository.SeatRepository;
import com.netpoint.ticketing.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationItemRepository reservationItemRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public PaymentResponseDTO pay(Long reservationId, PaymentRequestDTO dto) {

        // 1. Load reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new PaymentException("Reservation not found"));

        // 2. Ownership — only the customer who created it can pay
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User caller = userRepository.findByEmail(email)
                .orElseThrow(() -> new PaymentException("Authenticated user not found"));

        if (!reservation.getCustomer().getId().equals(caller.getId())) {
            throw new PaymentException("You can only pay for your own reservation");
        }

        // 3. State guards
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new PaymentException(
                    "Reservation is not pending (current status: " + reservation.getStatus() + ")");
        }
        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new PaymentException("Reservation has expired");
        }
        if (paymentRepository.findByReservationId(reservationId).isPresent()) {
            throw new PaymentException("Reservation already has a payment");
        }

        // 4. Load items + seats
        List<ReservationItem> items = reservationItemRepository.findByReservationId(reservationId);
        List<Seat> seats = new ArrayList<>();
        BigDecimal amount = BigDecimal.ZERO;
        for (ReservationItem item : items) {
            seats.add(item.getSeat());
            amount = amount.add(item.getPriceAtReservation());
        }

        // 5. Simulate payment
        boolean success = Boolean.TRUE.equals(dto.getSimulateSuccess());
        PaymentStatus paymentStatus = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        LocalDateTime now = LocalDateTime.now();

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(amount)
                .status(paymentStatus)
                .createdAt(now)
                .paidAt(success ? now : null)
                .build();
        payment = paymentRepository.save(payment);

        // 6. Apply business consequences
        if (success) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            for (Seat seat : seats) {
                seat.setStatus(SeatStatus.BOOKED);
            }
        } else {
            reservation.setStatus(ReservationStatus.CANCELLED);
            for (Seat seat : seats) {
                seat.setStatus(SeatStatus.AVAILABLE);
            }
        }
        reservationRepository.save(reservation);
        seatRepository.saveAll(seats);

        // 7. Respond
        PaymentResponseDTO out = new PaymentResponseDTO();
        out.setPaymentId(payment.getId());
        out.setReservationId(reservation.getId());
        out.setAmount(amount);
        out.setPaymentStatus(paymentStatus);
        out.setReservationStatus(reservation.getStatus());
        out.setPaidAt(payment.getPaidAt());
        out.setMessage(success
                ? "Payment successful, reservation confirmed"
                : "Payment failed, seats released");
        return out;
    }
}