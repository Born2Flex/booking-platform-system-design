package com.bookingplatform.catalog.venue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import java.util.Optional;

/**
 * A section of a venue: SEATED (has numbered seats) or STANDING (has a capacity).
 * The capacity lives in the standing_area table, mapped as a secondary table:
 * Hibernate writes that row only when capacity is non-null, so only for STANDING sections.
 */
@Entity
@SecondaryTable(name = "standing_area", pkJoinColumns = @PrimaryKeyJoinColumn(name = "section_id"))
public class VenueSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    private String name;

    @Enumerated(EnumType.STRING)
    private SectionKind kind;

    @Column(table = "standing_area")
    private Integer capacity;

    protected VenueSection() {
        // for Hibernate
    }

    private VenueSection(Venue venue, String name, SectionKind kind, Integer capacity) {
        this.venue = venue;
        this.name = Venue.requireText(name, "section name");
        this.kind = kind;
        this.capacity = capacity;
    }

    static VenueSection seated(Venue venue, String name) {
        return new VenueSection(venue, name, SectionKind.SEATED, null);
    }

    static VenueSection standing(Venue venue, String name, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Standing capacity must be positive, was " + capacity);
        }
        return new VenueSection(venue, name, SectionKind.STANDING, capacity);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public SectionKind kind() {
        return kind;
    }

    /** Present only for STANDING sections. */
    public Optional<Integer> capacity() {
        return Optional.ofNullable(capacity);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof VenueSection section && id != null && id.equals(section.id));
    }

    @Override
    public int hashCode() {
        return VenueSection.class.hashCode();
    }
}
