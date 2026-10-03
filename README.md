# Booking Platform: a system design sandbox

A ticketing/booking platform built stage by stage to practise system design with real tools.
Java 25 · Spring Boot 4.1 · Gradle 9 (Kotlin DSL) · Docker Compose.

## Layout

| Path | What lives there |
|------|------------------|
| `build-logic/` | Convention plugins: shared build config (`booking.java-conventions`, `booking.spring-service`) |
| `gradle/libs.versions.toml` | Version catalog: every dependency version in one place |
| `services/<name>/` | One deployable Spring Boot service each, with its own `Dockerfile` |
| `libs/` | Thin shared code only (message contracts, test helpers). Never shared domain models |
| `infra/` | `compose.yaml`, Postgres init, later observability configs and k8s manifests |
| `load-tests/` | k6 scripts |
| `docs/` | `ROADMAP.md`, ADRs, per-stage notes |

| Service | Port |
|---------|------|
| event-catalog-service | 8081 |
| booking-service | 8082 |
| postgres | 5432 |

## Everyday commands

```bash
./gradlew build                                   # compile + unit + integration tests + boot jars
./gradlew test                                    # fast unit tests only
./gradlew :services:booking-service:bootRun       # run one service locally
docker compose -f infra/compose.yaml up -d        # infra only (Postgres, ...)

./gradlew bootJar && docker compose -f infra/compose.yaml --profile apps up --build   # everything in containers
```

Inner dev loop: run infra in Docker, run the service you're editing from the IDE. Build images only to test the deployed system.
