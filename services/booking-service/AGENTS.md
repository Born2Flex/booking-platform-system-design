# booking-service

Owns **reservations**: holding seats, confirming and cancelling bookings. Write-heavy and correctness-critical:
two people must never get the same seat. Expect locking and idempotency (Stage 3) and a payment saga (Stage 6) here.

| | |
|---|---|
| Port | 8082 |
| Database | `booking` (user `booking`) on the shared Postgres |
| Package | `com.bookingplatform.booking` |
| Depends on | event-catalog-service (over HTTP, from Stage 3) |
| API | none yet: only `/actuator/health` |

## Rules
- Never trust seat data sent by the client; check it against the catalog.
- Every state-changing endpoint must be safe to retry (idempotency key) once the API exists.
- Booking states are a sealed type; every state transition is explicit and tested.
