package com.bookingplatform.catalog.venue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.bookingplatform.catalog.TestcontainersConfiguration;
import com.bookingplatform.catalog.venue.domain.SectionKind;
import com.bookingplatform.catalog.venue.domain.Venue;
import com.bookingplatform.catalog.venue.domain.VenueSeat;
import com.bookingplatform.catalog.venue.domain.VenueSection;
import com.bookingplatform.catalog.venue.infrastructure.VenueRepository;
import com.bookingplatform.catalog.venue.infrastructure.VenueSeatRepository;
import jakarta.persistence.EntityManager;
import java.time.ZoneId;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class VenuePersistenceTest {

    @Autowired
    VenueRepository venues;

    @Autowired
    VenueSeatRepository seats;

    @Autowired
    EntityManager entityManager;

    @Autowired
    JdbcClient jdbc;

    @Test
    void savesVenueWithSectionsAndReadsItBack() {
        var arena = new Venue("Arena", "Kyiv", ZoneId.of("Europe/Kyiv"));
        arena.addSeatedSection("A");
        arena.addStandingSection("Pit", 5_000);
        var id = venues.save(arena).id();
        flushAndClear();

        var loaded = venues.findById(id).orElseThrow();

        assertThat(loaded.timezone()).isEqualTo(ZoneId.of("Europe/Kyiv"));
        assertThat(loaded.sections())
                .extracting(VenueSection::name, VenueSection::kind, section -> section.capacity().orElse(null))
                .containsExactlyInAnyOrder(
                        tuple("A", SectionKind.SEATED, null),
                        tuple("Pit", SectionKind.STANDING, 5_000));
        // Only the standing section got a row in standing_area.
        assertThat(jdbc.sql("select count(*) from standing_area").query(Long.class).single()).isEqualTo(1);
    }

    @Test
    void savesSeatsOfASectionAndListsThemInOrder() {
        var arena = new Venue("Arena", "Kyiv", ZoneId.of("Europe/Kyiv"));
        var blockA = arena.addSeatedSection("A");
        venues.saveAndFlush(arena);

        seats.saveAll(IntStream.rangeClosed(1, 20).mapToObj(number -> new VenueSeat(blockA, "5", number)).toList());
        flushAndClear();

        var row5 = seats.findBySectionIdOrderByRowLabelAscSeatNumberAsc(blockA.id());

        assertThat(row5).hasSize(20);
        assertThat(row5).extracting(VenueSeat::seatNumber).startsWith(1, 2, 3).endsWith(20);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear(); // forget cached entities, so the next read really hits the database
    }
}
