-- Catalog schema: venues with their physical layout, and events with pricing.
-- Design notes: docs/stages/01-catalog.md

-- ---------------------------------------------------------------- venues

create table venue (
    id       bigint generated always as identity primary key,
    name     text not null,
    city     text not null,
    timezone text not null,                 -- IANA zone id, e.g. 'America/New_York'
    constraint venue_name_city_uk unique (name, city)
);

create index venue_city_idx on venue (city);

create table venue_section (
    id       bigint generated always as identity primary key,
    venue_id bigint not null references venue (id),
    name     text not null,
    kind     text not null check (kind in ('SEATED', 'STANDING')),
    constraint venue_section_name_uk unique (venue_id, name),
    -- Target for the composite foreign keys below: lets a child row demand a specific section kind.
    constraint venue_section_id_kind_uk unique (id, kind)
);

-- A seat may only exist in a SEATED section: the FK includes the kind, and the kind is pinned to 'SEATED'.
create table venue_seat (
    id           bigint generated always as identity primary key,
    section_id   bigint not null,
    section_kind text not null default 'SEATED' check (section_kind = 'SEATED'),
    row_label    text not null,
    seat_number  int  not null check (seat_number > 0),
    constraint venue_seat_section_fk foreign key (section_id, section_kind) references venue_section (id, kind),
    constraint venue_seat_position_uk unique (section_id, row_label, seat_number)
);

-- A standing area may only exist for a STANDING section (same trick).
create table standing_area (
    section_id   bigint primary key,
    section_kind text not null default 'STANDING' check (section_kind = 'STANDING'),
    capacity     int  not null check (capacity > 0),
    constraint standing_area_section_fk foreign key (section_id, section_kind) references venue_section (id, kind)
);

-- ---------------------------------------------------------------- events

create table event (
    id              bigint generated always as identity primary key,
    venue_id        bigint not null references venue (id),
    title           text not null,
    starts_at_local timestamp   not null,   -- what the organiser said, in the venue's timezone (source of truth)
    starts_at       timestamptz not null,   -- derived instant, for sorting and range queries
    status          text   not null default 'DRAFT' check (status in ('DRAFT', 'ON_SALE', 'CANCELLED')),
    version         bigint not null default 0   -- optimistic locking for organiser edits (low contention)
);

-- "What's on at this venue?" (also serves as the index for the venue_id foreign key)
create index event_venue_starts_at_idx on event (venue_id, starts_at);
-- "What's on sale soon?" Partial index: only ON_SALE rows, so it stays small.
create index event_on_sale_starts_at_idx on event (starts_at) where status = 'ON_SALE';

create table price_tier (
    id       bigint generated always as identity primary key,
    event_id bigint not null references event (id),
    name     text not null,
    price    numeric(12, 2) not null check (price >= 0),
    currency text not null check (currency ~ '^[A-Z]{3}$'),    -- ISO 4217, e.g. 'EUR'
    constraint price_tier_name_uk unique (event_id, name),
    constraint price_tier_id_event_uk unique (id, event_id)
);

-- Default price per section for an event. No row = section not on sale for this event.
create table section_tier (
    event_id      bigint not null references event (id),
    section_id    bigint not null references venue_section (id),
    price_tier_id bigint not null,
    primary key (event_id, section_id),
    -- The tier must belong to the same event.
    constraint section_tier_tier_fk foreign key (price_tier_id, event_id) references price_tier (id, event_id)
);

-- Per-seat exceptions (restricted view, VIP).
create table seat_price_override (
    event_id bigint not null references event (id),
    seat_id  bigint not null references venue_seat (id),
    price    numeric(12, 2) not null check (price >= 0),
    currency text not null check (currency ~ '^[A-Z]{3}$'),
    primary key (event_id, seat_id)
);

-- Seats blocked for one event (stage, camera, sound desk).
create table seat_kill (
    event_id bigint not null references event (id),
    seat_id  bigint not null references venue_seat (id),
    reason   text not null,
    primary key (event_id, seat_id)
);
