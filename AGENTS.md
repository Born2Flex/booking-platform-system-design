# AGENTS.md

Instructions for AI coding agents (and humans) working in this repo. Service-specific rules live in
`services/<name>/AGENTS.md`; the closest file to the code you're changing adds to this one.

## What this is
A **learning project**: a ticketing/booking platform built stage by stage to practise system design.
Roadmap: `docs/ROADMAP.md`. Per-stage notes: `docs/stages/`. Decisions: `docs/adr/`.

**Working with the developer:** go in small steps and explain *why*, not just *what*. Prefer letting the
developer implement the core pattern of a stage; scaffold around it. State trade-offs with numbers.

## Stack
Java 25 · Spring Boot 4.1 · Gradle 9 (Kotlin DSL, convention plugins in `build-logic/`) · Postgres 18 ·
Docker Compose. All versions live in `gradle/libs.versions.toml`; never hard-code a version in a module.

## Commands
```bash
./gradlew build                                  # compile + unit + integration tests + boot jars
./gradlew test                                   # unit tests only (fast)
./gradlew integrationTest                        # Spring / Testcontainers tests
./gradlew :services:<name>:bootRun               # run one service
docker compose -f infra/compose.yaml up -d       # infra only
./gradlew bootJar && docker compose -f infra/compose.yaml --profile apps up -d --build --wait   # everything
```
Run `./gradlew build` before committing; it must pass.

## Architecture rules
- **Services never depend on each other** in Gradle. They talk over HTTP or messages only.
- **Database per service.** A service only touches its own database; never read another service's tables.
- `libs/` holds only message contracts and test helpers. **No shared domain models.**
- New modules use a convention plugin (`booking.spring-service` for services, `booking.java-conventions` for libs).
- Package by feature, then by layer:
  ```
  com.bookingplatform.<service>.<feature>/
    api/             REST controllers + request/response DTOs
    domain/          business types and rules: NO Spring, NO persistence annotations
    application/     use cases, transactions, orchestration
    infrastructure/  repositories, messaging, HTTP clients
  ```
  Dependencies point inward: `api`, `infrastructure` → `application` → `domain`.

## Code style
- Use modern Java: **records** for DTOs and value objects, **sealed interfaces** for closed sets of
  states, **switch pattern matching** over them (no `default` branch on sealed types, so the compiler checks
  exhaustiveness), `var` when the type is obvious from the right-hand side.
- Immutable by default. Constructor injection only (no `@Autowired` fields). No Lombok.
- `Optional` only as a return type, never as a field or parameter.
- Validate at the edges (`jakarta.validation` on DTOs); domain constructors enforce their invariants.
- Money is never `double`: use `BigDecimal` or a `Money` value object.
- Time: `Instant` for moments, `LocalDate`/`ZonedDateTime` only where a calendar or zone matters.
- Logging via SLF4J with placeholders (`log.info("Booked {}", id)`); never `System.out`. Don't log secrets or personal data.
- Configuration in `application.yaml`; no hard-coded URLs, ports or credentials in code.
- Comments explain *why*, not *what*. Keep them rare.

## Tests
- `src/test` = **unit tests**: no Spring context, no containers, milliseconds each. Most tests live here.
- `src/integrationTest` = **integration tests**: `@SpringBootTest`, slices, Testcontainers.
- Real Postgres via **Testcontainers**, never H2: we want the real SQL dialect, locks and isolation behaviour.
- JUnit 5 + **AssertJ**. Name tests as behaviour: `reservesSeatWhenAvailable()`, not `testReserve1()`.
- Structure each test as given / when / then, separated by blank lines.
- Mock only what you don't own and can't run cheaply (e.g. an external payment provider). Don't mock repositories in
  integration tests or value objects anywhere.
- Every bug fix starts with a failing test that reproduces it.
- Concurrency claims (locking, idempotency) need a test that actually runs things in parallel.

## Git
- Small commits, one purpose each. Subject: imperative, short and descriptive, e.g. `Add seat hold expiry`.
- Commit and push to `main` when the build is green.

## Docs
- Each stage gets `docs/stages/NN-name.md`: problem → options → choice → trade-offs (with numbers) → how to break it.
- Significant, hard-to-reverse decisions get an ADR in `docs/adr/`.
- Update the service's `AGENTS.md` when its API, data or dependencies change.
