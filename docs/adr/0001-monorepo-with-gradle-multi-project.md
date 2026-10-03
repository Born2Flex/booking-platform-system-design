# ADR 0001: Monorepo with a Gradle multi-project build

**Status:** accepted · 2026-10-03

## Context
We want several microservices that can be built, tested and deployed independently, but a single
person (plus Claude) maintains everything and wants to refactor across services easily.

## Decision
One git repo, one Gradle build. Shared build config lives in convention plugins (`build-logic/`),
versions in a version catalog, and the Spring Boot BOM is applied as a Gradle `platform`.
Each service owns its own database, Dockerfile and deployment.

## Consequences
- ➕ Atomic cross-service changes, one place for versions, consistent builds.
- ➕ Gradle's up-to-date checks and build cache mean "build everything" costs about "build what changed".
- ➖ Easy to cheat on service boundaries (importing another service's classes). Mitigation: services never
  depend on each other in Gradle; only `libs/*` are shareable.
- ➖ In a big org, a monorepo needs tooling for ownership and CI scaling (affected-only builds, remote cache).
- Alternative considered: a repo per service. Better isolation and ownership, but version drift and
  painful cross-cutting changes. Not worth it for a single-developer sandbox.
