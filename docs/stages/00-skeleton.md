# Stage 0: Skeleton

## What we built
- Gradle 9.8 multi-project build: `build-logic` convention plugins + version catalog.
- Two empty Boot 4.1 services (`event-catalog-service`, `booking-service`) with Actuator health probes
  and virtual threads enabled.
- Test split: `src/test` (unit, fast) vs `src/integrationTest` (Spring context, containers).
- Per-service Dockerfile that packages the Gradle-built jar, using **layers** ordered by change frequency:
  `dependencies → spring-boot-loader → snapshot-dependencies → modules (our libs) → application`.
- `infra/compose.yaml`: one Postgres 18 with a separate database and user per service.

## Concepts to be able to explain
1. Why the Spring Boot BOM is a `platform(...)` and not the dependency-management plugin.
2. `implementation` vs `api`: what is *compile avoidance*, and why does `api` leak and slow builds?
3. Up-to-date check vs local build cache vs remote build cache: what does each one save?
4. Why image layer order matters: what gets re-pulled when one line of service code changes?
5. Database-per-service on one Postgres instance: what isolation do we get, and what don't we get?

## Exercise: measure the build (results 2026-10-04, laptop, Gradle 9.8)
| Scenario | Command | Time | Tasks |
|---|---|---|---|
| Clean build, cold daemon, no cache | `./gradlew clean build --no-build-cache` | 24.5 s | 14 executed |
| No-op rebuild | `./gradlew build` | 2.1 s | 12 up-to-date |
| One-line change in booking-service | edit a class, `./gradlew build` | 14.1 s | 4 executed, 8 up-to-date |
| Clean build, cache filled | `./gradlew clean build` | 2.2 s | 6 from cache (incl. integration tests) |
| One service, forced | `./gradlew :services:booking-service:build --rerun-tasks` | 13.5 s | 6 executed |

What the numbers say:
- **No-op = 2 s** is pure Gradle overhead; the configuration cache skips re-reading the build scripts.
- **One-line change**: catalog was entirely UP-TO-DATE. Only booking's compile, bootJar and integration
  test re-ran, and most of those 14 s is Spring starting inside the integration test.
  Even a comment counts: it shifts line numbers, which are stored in the .class file, so the bytes change.
- **Clean + cache**: `clean` deleted build/, yet compile *and integration tests* came FROM-CACHE: same inputs,
  so Gradle restored the stored outputs (including test results) instead of re-running them.
  `bootJar` and `processResources` always re-run: they're cheap file copies, so caching them isn't worth it.
- The expensive thing in this build is **starting Spring in tests**, not compiling. That's what we'll
  protect as the project grows (more unit tests, fewer full-context tests, Testcontainers reuse).

## How to break it
- Make `booking-service` depend on `event-catalog-service` in Gradle. What goes wrong architecturally?
- Swap the `COPY` order in the Dockerfile, change one line of code, rebuild the image twice, and compare the push size.
- Turn virtual threads off, add a `Thread.sleep(200)` endpoint, and load-test it (Stage 3 preview).
