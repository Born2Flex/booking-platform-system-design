package com.bookingplatform.catalog.venue.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * A physical seat. Its own entity (not a collection on the section) because a stadium has tens of thousands,
 * and loading a venue must never load every seat. References its section by id only.
 * The database guarantees the section is SEATED (composite foreign key in V1).
 */
@Entity
public class VenueSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sectionId;

    private String rowLabel;

    private int seatNumber;

    protected VenueSeat() {
        // for Hibernate
    }

    public VenueSeat(VenueSection section, String rowLabel, int seatNumber) {
        if (section.kind() != SectionKind.SEATED) {
            throw new IllegalArgumentException("Seats can only be added to a SEATED section, not " + section.name());
        }
        if (seatNumber <= 0) {
            throw new IllegalArgumentException("Seat number must be positive, was " + seatNumber);
        }
        this.sectionId = section.id();
        this.rowLabel = Venue.requireText(rowLabel, "row label");
        this.seatNumber = seatNumber;
    }

    public Long id() {
        return id;
    }

    public Long sectionId() {
        return sectionId;
    }

    public String rowLabel() {
        return rowLabel;
    }

    public int seatNumber() {
        return seatNumber;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof VenueSeat seat && id != null && id.equals(seat.id));
    }

    @Override
    public int hashCode() {
        return VenueSeat.class.hashCode();
    }
}
