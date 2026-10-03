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

## Exercise: measure the build (fill in your numbers)
| Scenario | Command | Time | Tasks executed |
|---|---|---|---|
| Clean build | `./gradlew clean build` | | |
| No-op rebuild | `./gradlew build` | | |
| One-line change in booking-service | edit a class, `./gradlew build` | | |
| Clean, but with build cache | `./gradlew clean build` again | | |
| Build one service | `./gradlew :services:booking-service:build` | | |

Add `--scan` to any of them to see where the time goes.
**Question:** in the "clean, with build cache" run, which tasks were FROM-CACHE and which still executed? Why?

## How to break it
- Make `booking-service` depend on `event-catalog-service` in Gradle. What goes wrong architecturally?
- Swap the `COPY` order in the Dockerfile, change one line of code, rebuild the image twice, and compare the push size.
- Turn virtual threads off, add a `Thread.sleep(200)` endpoint, and load-test it (Stage 3 preview).
