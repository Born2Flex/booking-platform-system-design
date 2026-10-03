# Roadmap

Each stage adds one concept and one tool, and ends with `docs/stages/NN-*.md`:
**problem → options → choice → trade-offs (with numbers) → how to break it**.

| # | Stage | Concepts | Tools | Java 25 / Boot 4 focus | Status |
|---|-------|----------|-------|------------------------|--------|
| 0 | Skeleton | Monorepo, build graph, image layers, build caching | Gradle 9, Docker | Virtual threads on; toolchains | 🟡 in progress |
| 1 | Catalog + DB | Schema design, indexes, pagination (offset vs keyset), N+1 | Postgres, Flyway, Testcontainers | Records, sealed types, pattern matching; JSpecify; `@ServiceConnection` | ⬜ |
| 2 | Caching | Cache-aside vs write-through, TTL, stampede, hot keys | Redis | Spring cache abstraction | ⬜ |
| 3 | Booking under contention | Optimistic vs pessimistic locking, idempotency keys, seat holds | Postgres, k6 | Virtual threads under load, pinning | ⬜ |
| 4 | Gateway + rate limiting | Token bucket vs sliding window, distributed limits | Spring Cloud Gateway, Redis | API versioning (Boot 4) | ⬜ |
| 5 | Async + events | Queue vs log, at-least-once, outbox, DLQ | Kafka | Stream gatherers; `libs/event-contracts` | ⬜ |
| 6 | Payments saga | Orchestration vs choreography, compensation | Kafka, payment-service | Sealed state machines | ⬜ |
| 7 | Observability | Metrics, traces, logs, SLOs | OpenTelemetry, Prometheus, Grafana | Scoped values for context; OTel starter | ⬜ |
| 8 | Resilience | Timeouts, retries, circuit breakers, bulkheads, chaos | Resilience4j, Toxiproxy | Structured concurrency; `@Retryable` / `@ConcurrencyLimit` | ⬜ |
| 9 | Search + read models | CQRS, eventual consistency | OpenSearch | HTTP interface clients | ⬜ |
| 10 | Scaling data | Replicas, sharding, consistent hashing | Postgres replica | AOT cache, compact headers, ZGC (measured) | ⬜ |

Later ideas: Kubernetes (kind), gRPC, WebSockets live seat map, Spring Modulith comparison, Jib/buildpacks vs Dockerfile.
