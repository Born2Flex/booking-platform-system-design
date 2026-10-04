# libs/

Shared code, kept deliberately thin. Allowed: message/event contracts, test helpers.
Not allowed: domain models, business logic, Spring configuration that services must share.
Sharing domain classes couples services into a distributed monolith.

Each lib applies `booking.java-conventions` and is depended on with `implementation`, never `api`,
unless its types appear in the consumer's public API.
