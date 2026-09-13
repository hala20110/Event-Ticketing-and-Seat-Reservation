package com.netpoint.ticketing.Model;

import com.netpoint.ticketing.Enum.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_seat_event_number",
                columnNames = {"event_id", "seat_number"}
        ),
        indexes = @Index(name = "idx_seat_event_status", columnList = "event_id, status")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;  // 1..N, unique per event

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status;

    @Version
    private Long version;  // optimistic locking
}