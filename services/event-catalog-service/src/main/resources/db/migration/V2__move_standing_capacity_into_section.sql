-- Standing capacity moves from its own table into venue_section.
-- A separate table for one column wasn't worth a join and a secondary-table mapping;
-- a CHECK constraint keeps the same guarantee. See docs/stages/01-catalog.md.

-- 1. Add the column (nullable: seated sections have no capacity).
alter table venue_section add column capacity int;

-- 2. Copy existing data, so the migration is safe on a database that already has venues.
update venue_section s
set    capacity = sa.capacity
from   standing_area sa
where  sa.section_id = s.id;

-- 3. Same guarantee as before: capacity is present (and positive) if and only if the section is STANDING.
alter table venue_section
    add constraint venue_section_capacity_ck
    check ((kind = 'STANDING') = (capacity is not null and capacity > 0));

-- 4. The old table is no longer needed.
drop table standing_area;
