package com.netpoint.ticketing.Repository;

import com.netpoint.ticketing.Model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}