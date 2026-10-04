# event-catalog-service

Owns **what can be booked**: venues, events, seat layouts and prices. Read-heavy: many people browse,
few people change the catalog. Expect caching (Stage 2) and search/read models (Stage 9) here.

| | |
|---|---|
| Port | 8081 |
| Database | `catalog` (user `catalog`) on the shared Postgres |
| Package | `com.bookingplatform.catalog` |
| Depends on | nothing (other services depend on it) |
| API | none yet: only `/actuator/health` |

## Rules
- List endpoints are always paginated.
