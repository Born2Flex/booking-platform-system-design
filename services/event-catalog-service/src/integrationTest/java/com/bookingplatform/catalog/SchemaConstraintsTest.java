package com.bookingplatform.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/** The database itself must reject invalid data, whatever the application code does. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional // each test rolls back
class SchemaConstraintsTest {

    @Autowired
    JdbcClient jdbc;

    long venueId;

    @BeforeEach
    void createVenue() {
        venueId = insertReturningId("insert into venue (name, city, timezone) values ('Arena', 'Kyiv', 'Europe/Kyiv') returning id");
    }

    @Test
    void acceptsSeatInSeatedSection() {
        var sectionId = insertSection("A", "SEATED");

        jdbc.sql("insert into venue_seat (section_id, row_label, seat_number) values (?, '5', 12)").param(sectionId).update();

        assertThat(jdbc.sql("select count(*) from venue_seat").query(Long.class).single()).isEqualTo(1);
    }

    @Test
    void rejectsSeatInStandingSection() {
        var pitId = insertSection("Pit", "STANDING", 5_000);

        assertThatThrownBy(() -> jdbc.sql("insert into venue_seat (section_id, row_label, seat_number) values (?, '1', 1)")
                .param(pitId).update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateSeatPosition() {
        var sectionId = insertSection("A", "SEATED");
        jdbc.sql("insert into venue_seat (section_id, row_label, seat_number) values (?, '5', 12)").param(sectionId).update();

        assertThatThrownBy(() -> jdbc.sql("insert into venue_seat (section_id, row_label, seat_number) values (?, '5', 12)")
                .param(sectionId).update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsPriceTierFromAnotherEvent() {
        var sectionId = insertSection("A", "SEATED");
        var friday = insertEvent("Friday concert");
        var saturday = insertEvent("Saturday game");
        var saturdayTier = insertReturningId(
                "insert into price_tier (event_id, name, price, currency) values (" + saturday + ", 'Premium', 100, 'EUR') returning id");

        assertThatThrownBy(() -> jdbc.sql("insert into section_tier (event_id, section_id, price_tier_id) values (?, ?, ?)")
                .params(friday, sectionId, saturdayTier).update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsStandingSectionWithoutCapacity() {
        assertThatThrownBy(() -> insertSection("Pit", "STANDING", null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsSeatedSectionWithCapacity() {
        assertThatThrownBy(() -> insertSection("A", "SEATED", 100))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private long insertSection(String name, String kind) {
        return insertSection(name, kind, null);
    }

    private long insertSection(String name, String kind, Integer capacity) {
        return jdbc.sql("insert into venue_section (venue_id, name, kind, capacity) values (?, ?, ?, ?) returning id")
                .params(venueId, name, kind, capacity).query(Long.class).single();
    }

    private long insertEvent(String title) {
        return jdbc.sql("""
                insert into event (venue_id, title, starts_at_local, starts_at)
                values (?, ?, '2027-03-14 20:00', '2027-03-14 18:00Z') returning id""")
                .params(venueId, title).query(Long.class).single();
    }

    private long insertReturningId(String sql) {
        return jdbc.sql(sql).query(Long.class).single();
    }
}
