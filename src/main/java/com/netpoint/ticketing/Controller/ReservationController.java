package com.netpoint.ticketing.Controller;

import com.netpoint.ticketing.DTO.ReservationRequestDTO;
import com.netpoint.ticketing.DTO.ReservationResponseDTO;
import com.netpoint.ticketing.Service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @PostMapping
    public ResponseEntity<ReservationResponseDTO> create(@Valid @RequestBody ReservationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(dto));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponseDTO>> my() {
        return ResponseEntity.ok(reservationService.myReservations());
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }
}