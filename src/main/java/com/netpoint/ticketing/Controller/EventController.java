package com.netpoint.ticketing.Controller;

import com.netpoint.ticketing.DTO.EventRequestDTO;
import com.netpoint.ticketing.DTO.EventResponseDTO;
import com.netpoint.ticketing.DTO.SeatResponseDTO;
import com.netpoint.ticketing.Service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @PostMapping
    public ResponseEntity<EventResponseDTO> create(@Valid @RequestBody EventRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(dto));
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> list() {
        return ResponseEntity.ok(eventService.listEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatResponseDTO>> seats(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventSeats(id));
    }

    @GetMapping("/{id}/seats/available")
    public ResponseEntity<List<SeatResponseDTO>> available(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getAvailableSeats(id));
    }
}