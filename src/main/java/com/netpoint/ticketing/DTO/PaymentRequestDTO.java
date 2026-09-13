package com.netpoint.ticketing.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDTO {


    @NotNull(message = "simulateSuccess is required")
    private Boolean simulateSuccess;
}