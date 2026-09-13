package com.netpoint.ticketing.DTO;

import com.netpoint.ticketing.Enum.PaymentStatus;
import com.netpoint.ticketing.Enum.ReservationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDTO {
    private Long paymentId;
    private Long reservationId;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private ReservationStatus reservationStatus;
    private LocalDateTime paidAt;
    private String message;
}