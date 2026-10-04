-- Database per service: one Postgres instance (to save laptop memory),
-- but each service gets its own database and user and never touches the other's.
CREATE USER catalog WITH PASSWORD 'catalog';
CREATE DATABASE catalog OWNER catalog;

CREATE USER booking WITH PASSWORD 'booking';
CREATE DATABASE booking OWNER booking;

-- Postgres grants CONNECT to PUBLIC (every user) by default. Only the owner may connect.
REVOKE CONNECT ON DATABASE catalog FROM PUBLIC;
REVOKE CONNECT ON DATABASE booking FROM PUBLIC;
