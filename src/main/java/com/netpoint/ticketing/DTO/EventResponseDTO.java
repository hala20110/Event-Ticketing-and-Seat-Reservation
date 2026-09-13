package com.netpoint.ticketing.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Long venueId;
    private String venueName;
    private Long organizerId;
    private String organizerName;
    private LocalDateTime eventDate;
    private BigDecimal ticketPrice;
    private Integer seatCapacity;
    private Long availableSeats;   // filled in by service
}