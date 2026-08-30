# 0001: Keep JobRunr on its auto-detected MySQL storage instead of adding Redis storage

## Status
Accepted

## Context
`coupon-crawler-service` and `coupon-api-service` use JobRunr to enqueue and process
background coupon-validation jobs. Every `application*.yml` in both modules configured
JobRunr under `org.jobrunr.*` (`background-job-server.enabled`, `dashboard.enabled`, and
a `storage.type: redis` block pointing at the project's existing Redis instance) — the
intent was clearly to run job storage on Redis, matching the rest of the stack's caching
layer.

In practice, the crawler ran its discovery loop successfully and enqueued jobs, but
nothing ever picked them up: `coupon_course_data` stayed empty and `scraping_task_logs`
had zero rows, even though the crawl itself logged success. Inspecting the job storage
directly (`jobrunr_jobs` in MySQL — not Redis) showed 33 jobs permanently stuck in
`ENQUEUED`, and `jobrunr_backgroundjobservers` (the worker heartbeat table) had never had
a single row written to it — no `BackgroundJobServer` had ever actually started, in any
run, in either service.

Reading the JobRunr Spring Boot 3 starter's source directly
(`org.jobrunr.spring.autoconfigure.JobRunrProperties`) showed why:
`@ConfigurationProperties(prefix = "jobrunr")` — not `org.jobrunr`. Every property this
project had under `org: jobrunr: ...` was silently unbound (Spring's relaxed binding
just ignores unmatched YAML, it doesn't fail the build). `background-job-server.enabled`
never reached the real `jobrunr.background-job-server.enabled` property, so
`@ConditionalOnProperty` never created the `BackgroundJobServer` bean — jobs could be
enqueued (the `JobScheduler` bean has `matchIfMissing = true` and started regardless) but
never processed. Likewise the `storage.type: redis` key doesn't exist at all (the real
property is `database.type`), so JobRunr fell back to auto-detecting a storage provider
from the existing Spring-managed MySQL `DataSource` — explaining why `jobrunr_jobs`
existed in MySQL, not Redis, despite the YAML's intent.

Fixing the prefix (`org.jobrunr` → `jobrunr`) alone would restore the background worker.
That still leaves an open question: now that storage config is actually read, should
`database.type` be explicitly set to `redis`?

## Decision
Fix the property prefix so `background-job-server.enabled`/`dashboard.enabled` actually
take effect, and drop the storage block entirely rather than pointing it at Redis.
JobRunr keeps auto-detecting the existing MySQL `DataSource` as its storage provider.

Rationale: there is no Redis storage-provider dependency on the classpath (only
`jobrunr-spring-boot-3-starter`, which bundles SQL support but not a Jedis/Lettuce-backed
NoSQL provider) — using Redis for real would mean adding a new dependency for a backend
that was never actually exercised. The SQL-backed storage was already working end to end
(Flyway-created `jobrunr_*` tables, correct `ENQUEUED` rows) the moment the prefix bug is
fixed; there's no concrete pain point (throughput, latency, ops burden) that Redis would
solve here that MySQL doesn't already handle fine at this project's job volume.

## Consequences
- No new dependency, no new moving part in `docker-compose*.yml` to keep in sync for job
  storage specifically — MySQL was already a hard requirement for the domain data anyway.
- Job storage rows (`jobrunr_jobs`, `jobrunr_backgroundjobservers`, etc.) live in the same
  MySQL database as domain data, queryable with the same `mysql` client/tooling used for
  everything else — no second system to reason about when debugging a stuck job.
- Redis remains configured and used elsewhere in the stack (via `RedisService`, e.g.
  `LastFetchTimeManager`'s `last_fetch_time` key) but is **not** used for JobRunr — a
  reader skimming `application.yml` and seeing a Redis connection configured nearby could
  reasonably assume JobRunr uses it too; this ADR is the answer when that assumption is
  questioned again later.
- Job polling adds a small amount of extra write/read load to MySQL (JobRunr's background
  server polls its own tables on an interval) instead of Redis, which would have handled
  that polling pattern with less per-operation overhead — not a real concern at this
  project's job volume (tens of jobs per crawl round), but worth revisiting if job volume
  grows by orders of magnitude.
