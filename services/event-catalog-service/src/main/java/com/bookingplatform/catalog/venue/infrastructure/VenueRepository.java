package com.bookingplatform.catalog.venue.infrastructure;

import com.bookingplatform.catalog.venue.domain.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
