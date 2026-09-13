package com.netpoint.ticketing.Service;

import com.netpoint.ticketing.DTO.EventRequestDTO;
import com.netpoint.ticketing.DTO.EventResponseDTO;
import com.netpoint.ticketing.DTO.SeatResponseDTO;
import com.netpoint.ticketing.DTO.VenueRequestDTO;
import com.netpoint.ticketing.Enum.SeatStatus;
import com.netpoint.ticketing.Model.Event;
import com.netpoint.ticketing.Model.Seat;
import com.netpoint.ticketing.Model.User;
import com.netpoint.ticketing.Model.Venue;
import com.netpoint.ticketing.Repository.EventRepository;
import com.netpoint.ticketing.Repository.SeatRepository;
import com.netpoint.ticketing.Repository.UserRepository;
import com.netpoint.ticketing.Repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    @Autowired private VenueRepository venueRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private UserRepository userRepository;

    // Venues

    public Venue createVenue(VenueRequestDTO dto) {
        Venue venue = Venue.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .city(dto.getCity())
                .capacity(dto.getCapacity())
                .build();
        return venueRepository.save(venue);
    }

    public List<Venue> listVenues() {
        return venueRepository.findAll();
    }

    // Events


    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO dto) {

        Venue venue = venueRepository.findById(dto.getVenueId())
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        if (dto.getSeatCapacity() > venue.getCapacity()) {
            throw new RuntimeException("Seat capacity exceeds venue capacity");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        Event event = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .venue(venue)
                .organizer(organizer)
                .eventDate(dto.getEventDate())
                .ticketPrice(dto.getTicketPrice())
                .seatCapacity(dto.getSeatCapacity())
                .build();

        event = eventRepository.save(event);

        // Auto-generate seats 1..seatCapacity
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= dto.getSeatCapacity(); i++) {
            seats.add(Seat.builder()
                    .event(event)
                    .seatNumber(i)
                    .status(SeatStatus.AVAILABLE)
                    .build());
        }
        seatRepository.saveAll(seats);

        return toResponse(event);
    }

    public List<EventResponseDTO> listEvents() {
        return eventRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponseDTO getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return toResponse(event);
    }

    public List<SeatResponseDTO> getEventSeats(Long eventId) {
        // throws if event doesn't exist — good, avoids returning [] for a typo'd id
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found");
        }
        return seatRepository.findByEventId(eventId).stream()
                .map(s -> new SeatResponseDTO(s.getId(), s.getSeatNumber(), s.getStatus()))
                .toList();
    }

    public List<SeatResponseDTO> getAvailableSeats(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found");
        }
        return seatRepository.findByEventIdAndStatus(eventId, SeatStatus.AVAILABLE).stream()
                .map(s -> new SeatResponseDTO(s.getId(), s.getSeatNumber(), s.getStatus()))
                .toList();
    }

    // Mapping

    private EventResponseDTO toResponse(Event event) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setVenueId(event.getVenue().getId());
        dto.setVenueName(event.getVenue().getName());
        dto.setOrganizerId(event.getOrganizer().getId());
        dto.setOrganizerName(event.getOrganizer().getName());
        dto.setEventDate(event.getEventDate());
        dto.setTicketPrice(event.getTicketPrice());
        dto.setSeatCapacity(event.getSeatCapacity());
        dto.setAvailableSeats(
                seatRepository.countByEventIdAndStatus(event.getId(), SeatStatus.AVAILABLE)
        );
        return dto;
    }
}