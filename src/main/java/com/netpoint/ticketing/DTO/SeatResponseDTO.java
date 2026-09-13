package com.netpoint.ticketing.DTO;

import com.netpoint.ticketing.Enum.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatResponseDTO {
    private Long id;
    private Integer seatNumber;
    private SeatStatus status;
}