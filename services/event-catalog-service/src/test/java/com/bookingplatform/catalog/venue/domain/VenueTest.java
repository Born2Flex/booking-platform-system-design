package com.bookingplatform.catalog.venue.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class VenueTest {

    private final Venue arena = new Venue("Arena", "Kyiv", ZoneId.of("Europe/Kyiv"));

    @Test
    void standingSectionHasCapacityAndSeatedSectionDoesNot() {
        var pit = arena.addStandingSection("Pit", 5_000);
        var blockA = arena.addSeatedSection("A");

        assertThat(pit.capacity()).contains(5_000);
        assertThat(blockA.capacity()).isEmpty();
        assertThat(arena.sections()).containsExactly(pit, blockA);
    }

    @Test
    void rejectsDuplicateSectionNameIgnoringCase() {
        arena.addSeatedSection("A");

        assertThatThrownBy(() -> arena.addSeatedSection("a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already has a section");
    }

    @Test
    void rejectsNonPositiveStandingCapacity() {
        assertThatThrownBy(() -> arena.addStandingSection("Pit", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsSeatInStandingSection() {
        var pit = arena.addStandingSection("Pit", 5_000);

        assertThatThrownBy(() -> new VenueSeat(pit, "1", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SEATED");
    }

    @Test
    void sectionsCannotBeModifiedFromOutside() {
        assertThatThrownBy(() -> arena.sections().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
