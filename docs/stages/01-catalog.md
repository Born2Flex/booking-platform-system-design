# Stage 1: Catalog with a real database

Status: designing the schema. Implementation not started.

## Decisions so far

### Who owns what about a "seat"
| Concept | Example | Owner |
|---|---|---|
| Physical seat | Arena, A-5-12 exists | catalog (venue layout) |
| Seat offered for an event | Friday: A-5-12 on sale, tier Premium | catalog (event setup) |
| Seat availability | Friday: A-5-12 HELD / SOLD | booking |

Consequence: booking needs seat and price data it doesn't own. Stage 3 asks catalog synchronously
(simple, but booking depends on catalog being up). Stage 5 replicates it via events (resilient, eventually consistent).
A booking always stores a **snapshot** of seat label and price.

### Pricing: section tiers + per-seat overrides
Default price comes from the section's tier; exceptions (restricted view, VIP) via `seat_price_override`.
Lookup: override if present, else section tier. Cheap to set up, still flexible.

### Time: local time + zone is the truth for future events
`starts_at_local` + `venue.timezone` is what the organiser means ("8 pm in New York").
`starts_at timestamptz` is derived for sorting and range queries, and recalculated if timezone rules change.
Past facts (`booked_at`, `paid_at`) are plain `timestamptz`.

### Seated vs standing: separate tables
`venue_seat` (row, number) and `standing_area` (capacity), so every column is NOT NULL with real constraints.
Cost: "all inventory of an event" needs two queries or a UNION.

### Layout per venue, events subtract from it ("seat kills")
Physical layout is defined once per venue. Per event: a section without a `section_tier` row isn't on sale;
single seats are blocked via `seat_kill(event_id, seat_id, reason)`.
Alternative not taken (yet): named venue configurations, worth it only if many events share the same blocked areas.

## Draft schema
```
venue               (id, name, city, timezone)
venue_section       (id, venue_id, name, kind)          kind: SEATED | STANDING
venue_seat          (id, section_id, row_label, seat_number)
standing_area       (section_id, capacity)

event               (id, venue_id, title, starts_at_local, starts_at, status)   status: DRAFT | ON_SALE | CANCELLED
price_tier          (id, event_id, name, price, currency)
section_tier        (event_id, section_id, price_tier_id)
seat_price_override (event_id, seat_id, price, currency)
seat_kill           (event_id, seat_id, reason)
```

## Parked for Stage 3: overbooking and hot rows
- Check-then-act race: read count → check → write lets two buyers take the last ticket.
- Fix: one atomic statement, `UPDATE ... SET sold = sold + :qty WHERE id = :id AND sold + :qty <= capacity`,
  then check rows affected; plus `CHECK (sold <= capacity)` as a safety net.
- Hot row: one row handles roughly 1 / lock-hold-time updates per second (~500/s at 2 ms). A big on-sale
  (~16,000/s) queues up, every waiter holds a DB connection, and the pool starves for **all** endpoints.
- Defences, cheapest first: tiny transactions (never call payment inside), fail fast (`lock_timeout`, short pool timeout),
  bulkhead for the buy endpoint, split counters, counter in Redis, virtual waiting room.
- Optimistic locking is wrong under high contention: the losers retry and hammer the same row (retry storm).
  Good for low contention (editing an event description).
