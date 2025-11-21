# Getting Started

## Overview
- `Spring Boot Coupon Server` now ships as a multi-module monorepo:
  - `modules/coupon-domain`: shared entities, DTOs, repositories, and Flyway migrations.
  - `modules/coupon-api-service`: REST controllers, auth/security, OpenAPI.
  - `modules/coupon-crawler-service`: scheduled crawlers and validators.
- Core stack: Java 17, Spring Boot 3.1, MySQL, Flyway, Gradle, Docker/Compose.
- Key docs: `README.md`, `docs/business-logic.md`, `CONTRIBUTING.md`.

## Prerequisites
- Java 17 JDK in your `$PATH`.
- Docker Desktop (or compatible) for running MySQL; Docker Compose v2+ is recommended.
- Make sure ports `3306` (MySQL) and `8080` (Spring Boot default) are free.

## Bootstrap the Project
```bash
git clone https://github.com/huythanh0x/udemy_coupon_server_java_spring
cd udemy_coupon_server_java_spring
```

### Option A: Full stack via Docker Compose (recommended)
```bash
docker compose up
```
This brings up the MySQL container plus the Spring Boot service with the default `application.properties` profile (DB host is `mysql`).

### Option B: Run the API locally, keep MySQL in Docker
```bash
docker compose -f docker-compose.local.yml up -d mysql
./gradlew :modules:coupon-api-service:bootRun --args='--spring.profiles.active=local'
```

### Run the crawler service locally (optional but recommended)
```bash
docker compose -f docker-compose.local.yml up -d mysql
./gradlew :modules:coupon-crawler-service:bootRun --args='--spring.profiles.active=local'
```
The `local` profile (`src/main/resources/application-local.properties`) targets `jdbc:mysql://localhost:3306/training_coupon` and bumps crawler thread counts for richer local validation.

## Database & Migrations
- Flyway runs automatically on application startup; migration scripts live under `src/main/resources/db/migration`.
- To force-run migrations once MySQL is up: `./gradlew flywayMigrate`.

## Configuration Profiles
| Profile file | Purpose | Highlights |
| ------------ | ------- | ---------- |
| `application.properties` | Default/prod-like | MySQL host `mysql`, crawler threads `custom.number-of-request-thread=4`, JWT TTL 24h. |
| `application-local.properties` | Developer workflow | MySQL host `localhost`, crawler threads `10`, `custom.number-of-real-discount-coupon=0` for quicker loops. |

You can override any property via `--args='--spring.profiles.active=<profile>'` or environment variables (Spring Boot relaxed binding).

## Useful Gradle Tasks
- `./gradlew :modules:coupon-api-service:bootRun` – start the API service.
- `./gradlew :modules:coupon-crawler-service:bootRun` – start the crawler worker.
- `./gradlew :modules:coupon-api-service:bootJar` / `:modules:coupon-crawler-service:bootJar` – package runnable jars.
- `./gradlew build` – compile all modules.
- `./gradlew test` – runs the test suite (currently empty but kept for future coverage).

## API Surface
- Start the API service and browse `http://localhost:8080/swagger-ui/index.html` for interactive docs (backed by Springdoc/OpenAPI).
- Raw OpenAPI JSON lives at `http://localhost:8080/v3/api-docs`.
- The crawler service exposes only health endpoints by default and listens on `8081` (configurable via `server.port`).
- Generated reports/tests are emitted under `build/reports/*` after Gradle runs.

## Troubleshooting Checklist
- MySQL unreachable: ensure the container is healthy (`docker ps`) and credentials match the selected profile.
- Flyway validation errors: drop the schema (only in dev) or fix the offending migration checksum.
- Crawler appears idle: confirm `custom.interval-time` (default 900 000 ms) and watch logs for `Wait for ... milliseconds`.

## Contributing
- Follow the workflow in `CONTRIBUTING.md` (issue triage, fork/branch, code style).
- Always run `./gradlew test` (even if empty) before opening a pull request, so future suites remain green.

You’re ready to build! Spin up Docker, run the API, and hit `GET /api/v1/coupons` to validate data flow end-to-end.

