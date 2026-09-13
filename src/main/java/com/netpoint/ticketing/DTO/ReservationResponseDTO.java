package com.netpoint.ticketing.DTO;

import com.netpoint.ticketing.Enum.ReservationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReservationResponseDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long eventId;
    private String eventTitle;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private List<ReservedSeatDTO> seats;
    private BigDecimal totalPrice;

    @Data
    public static class ReservedSeatDTO {
        private Long seatId;
        private Integer seatNumber;
        private BigDecimal price;
    }
}