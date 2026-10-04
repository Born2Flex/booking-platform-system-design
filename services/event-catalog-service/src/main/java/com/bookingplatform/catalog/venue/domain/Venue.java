package com.bookingplatform.catalog.venue.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A place where events happen. Owns its sections (a small, bounded list).
 * Seats are NOT a collection here: a stadium has tens of thousands, see {@link VenueSeat}.
 */
@Entity
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String city;

    @Convert(converter = ZoneIdConverter.class)
    private ZoneId timezone;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VenueSection> sections = new ArrayList<>();

    protected Venue() {
        // for Hibernate
    }

    public Venue(String name, String city, ZoneId timezone) {
        this.name = requireText(name, "name");
        this.city = requireText(city, "city");
        this.timezone = Objects.requireNonNull(timezone, "timezone");
    }

    public VenueSection addSeatedSection(String name) {
        return addSection(VenueSection.seated(this, name));
    }

    public VenueSection addStandingSection(String name, int capacity) {
        return addSection(VenueSection.standing(this, name, capacity));
    }

    private VenueSection addSection(VenueSection section) {
        if (sections.stream().anyMatch(existing -> existing.name().equalsIgnoreCase(section.name()))) {
            throw new IllegalArgumentException("Venue already has a section named " + section.name());
        }
        sections.add(section);
        return section;
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String city() {
        return city;
    }

    public ZoneId timezone() {
        return timezone;
    }

    public List<VenueSection> sections() {
        return Collections.unmodifiableList(sections);
    }

    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.strip();
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof Venue venue && id != null && id.equals(venue.id));
    }

    @Override
    public int hashCode() {
        return Venue.class.hashCode();
    }
}
