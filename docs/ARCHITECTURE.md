# Architecture

Target architecture. Solid boxes exist today; dashed boxes are planned (stage in brackets).

```mermaid
flowchart LR
    user([Browser / mobile app])

    subgraph edge [Edge]
        gw[API Gateway<br/>routing, rate limiting,<br/>waiting room · S4]
    end

    subgraph services [Services]
        catalog[event-catalog-service<br/>venues, events, prices]
        booking[booking-service<br/>seat holds, bookings]
        payment[payment-service<br/>fake PSP · S6]
        notif[notification-service<br/>emails, tickets · S5]
        checkin[check-in-service<br/>ticket scans at the gate · later]
    end

    subgraph data [Data stores: one owner each]
        pgc[(Postgres<br/>catalog)]
        pgb[(Postgres<br/>booking)]
        pgp[(Postgres<br/>payment · S6)]
        redis[(Redis<br/>cache, holds, limits · S2-S4)]
        mongo[(MongoDB<br/>notifications · S5)]
        os[(OpenSearch<br/>event search · S9)]
        cass[(Cassandra<br/>scan log · later)]
    end

    kafka{{Kafka<br/>domain events · S5}}
    obs[[OpenTelemetry → Prometheus,<br/>Grafana, Jaeger · S7]]

    user --> gw
    gw --> catalog
    gw --> booking
    gw -.-> redis
    booking -- "HTTP: seat + price lookup (S3)" --> catalog
    catalog --> pgc
    catalog -.-> redis
    booking --> pgb
    booking -.-> redis
    catalog -- EventPublished --> kafka
    booking -- BookingRequested / Confirmed --> kafka
    kafka --> payment
    payment -- PaymentSucceeded / Failed --> kafka
    kafka --> booking
    kafka --> notif
    kafka --> os
    kafka --> checkin
    payment --> pgp
    notif --> mongo
    checkin --> cass
    services -.-> obs

    classDef planned stroke-dasharray: 5 5;
    class gw,payment,notif,checkin,pgp,redis,mongo,os,cass,kafka,obs planned;
```

## Where each kind of database fits, and why

| Store | Type | Used for | Why this one |
|---|---|---|---|
| **Postgres** | Relational | catalog, booking, payment | Transactions, constraints, joins. Overbooking is prevented *by the database* (constraints, atomic updates) |
| **Redis** | Key-value, in memory | Catalog cache, seat holds with TTL, rate-limit counters, hot ticket counters | Microsecond reads, atomic `INCR`/`DECRBY`, keys that expire by themselves. Data is mostly rebuildable |
| **MongoDB** | Document | Notification templates and delivery log | Each channel (email, SMS, push) has a different shape; documents are read and written whole, never joined |
| **OpenSearch** | Search index | "Taylor Swift concerts in Europe next month" | Full-text search, filters and facets that SQL `LIKE` can't do well. A read model built from Kafka events |
| **Cassandra** | Wide-column | Ticket scans at venue gates | Huge write bursts (50,000 scans in 30 min), append-only, always read by key (event + ticket). Scales writes horizontally |

Rule of thumb: **start relational; add a specialised store when an access pattern clearly needs it** and you can name
the trade-off (usually weaker transactions or eventual consistency in exchange for speed or scale).
Each store has exactly one owning service. Others get the data through APIs or events.
