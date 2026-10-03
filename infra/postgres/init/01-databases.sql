-- Database per service: one Postgres instance (to save laptop memory),
-- but each service gets its own database and user and never touches the other's.
CREATE USER catalog WITH PASSWORD 'catalog';
CREATE DATABASE catalog OWNER catalog;

CREATE USER booking WITH PASSWORD 'booking';
CREATE DATABASE booking OWNER booking;
