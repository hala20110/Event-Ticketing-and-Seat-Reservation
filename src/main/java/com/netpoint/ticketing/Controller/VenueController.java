package com.netpoint.ticketing.Controller;

import com.netpoint.ticketing.DTO.VenueRequestDTO;
import com.netpoint.ticketing.Model.Venue;
import com.netpoint.ticketing.Service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    @Autowired
    private EventService eventService;

    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @PostMapping
    public ResponseEntity<Venue> create(@Valid @RequestBody VenueRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createVenue(dto));
    }

    @GetMapping
    public ResponseEntity<List<Venue>> list() {
        return ResponseEntity.ok(eventService.listVenues());
    }
}