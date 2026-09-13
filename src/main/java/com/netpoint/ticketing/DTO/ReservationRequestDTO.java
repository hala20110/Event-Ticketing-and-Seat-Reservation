package com.netpoint.ticketing.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReservationRequestDTO {

    @NotNull(message = "Event id is required")
    private Long eventId;

    @NotEmpty(message = "You must reserve at least one seat")
    private List<Long> seatIds;
}