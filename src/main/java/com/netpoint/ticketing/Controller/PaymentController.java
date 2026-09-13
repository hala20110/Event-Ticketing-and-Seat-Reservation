package com.netpoint.ticketing.Controller;

import com.netpoint.ticketing.DTO.PaymentRequestDTO;
import com.netpoint.ticketing.DTO.PaymentResponseDTO;
import com.netpoint.ticketing.Service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentResponseDTO> pay(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.ok(paymentService.pay(id, dto));
    }
}