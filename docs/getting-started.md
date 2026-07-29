# Getting Started

## Overview
`Course Deal Server` is a modern, asynchronous multi-module backend designed for high performance and security.

```mermaid
graph TD
    API[coupon-api-service] --> Domain[coupon-domain]
    API --> Common[coupon-common]
    API --> Infra[coupon-infrastructure]
    API --> Scraper[course-scraper]
    API --> ExtApi[course-external-api]
    API --> ID[identity-service]

    Crawler[coupon-crawler-service] --> Domain
    Crawler --> Common
    Crawler --> Infra
    Crawler --> Scraper

    ID --> Domain
    ID --> Common
    ID --> Infra

    ExtApi --> Scraper
    ExtApi --> Common

    Scraper --> Domain
    Scraper --> Common
    Scraper --> Notify[notification-service]

    Notify --> Domain
    Notify --> Common

    Infra --> Domain

    subgraph "External Dependencies"
        Domain -- "Flyway" --> MySQL[(MySQL)]
        Infra -- "Cache/Job Storage" --> Redis((Redis))
        Notify -- "Push Alerts" --> FCM((Firebase))
        Scraper -- "Validation" --> Ext((Course Providers))
    end
```

### Module Breakdown:
- **`modules/coupon-common`**: Cross-cutting utilities (logging helper, shared exceptions) used by nearly every other module.
- **`modules/coupon-domain`**: Pure data layer. Entities are mapped to MySQL via JPA.
- **`modules/coupon-infrastructure`**: Redis caching/config only.
- **`modules/notification-service`**: Firebase Admin SDK (FCM) push notifications.
- **`modules/course-scraper`**: Coupon scraping/validation pipeline shared by both deployables (JobRunr-backed).
- **`modules/course-external-api`**: Client for Udemy's public course API, used only by `coupon-api-service`.
- **`modules/identity-service`**: Standalone authentication (Social/Passkey) and user preferences.
- **`modules/coupon-api-service`**: Main REST entry point for course searching and details.
- **`modules/coupon-crawler-service`**: Periodic discovery workers and background job worker.

## Prerequisites
- Java 17 JDK in your `$PATH`.
- Docker Desktop (or compatible) for MySQL and Redis.
- Ports `3306` (MySQL), `6379` (Redis), `8080` (API), and `8081` (Crawler) should be available.

## Bootstrap the Project
```bash
git clone https://github.com/huythanh0x/course-deal-server
cd course-deal-server
```

### Option A: Production Stack via Docker
```bash
docker compose -f docker-compose.prod.yml up
```
Starts the entire backend using pre-built images. Requires a `.env` file with production secrets.

### Option B: Local Development
1. Start data containers:
```bash
docker compose -f docker-compose.local.yml up -d
```
2. Run services from source:
```bash
./gradlew :modules:coupon-api-service:bootRun --args='--spring.profiles.active=local'
# In a separate terminal
./gradlew :modules:coupon-crawler-service:bootRun --args='--spring.profiles.active=local'
```

## Database Migrations
- Schema changes and seed data are managed by [Flyway](https://flywaydb.org/).
- Migration scripts live under `modules/coupon-domain/src/main/resources/db/migration` (e.g., `V1__init_schema.sql`).
- When the Spring Boot app starts it automatically runs pending migrations; no manual SQL is required.
- For local verification you can run `./gradlew :modules:coupon-api-service:flywayMigrate` (or the crawler equivalent) once MySQL is up.

## Configuration (YAML)
We use hierarchical YAML files for easier management of complex settings.

| File | Purpose | Key Knobs |
| ------------ | ------- | ---------- |
| `application.yml` | Base Config | `spring.datasource`, `org.jobrunr` |
| `application-local.yml` | Dev Environment | `custom.async.scraper.max-pool-size=10`, `jwt-secret`, `logging.level.org.springframework.web=DEBUG` |

### Modern Auth Setup
To enable the full feature set, define these in your environment:
- `GOOGLE_CLIENT_ID`: For social login validation.
- `FIREBASE_CONFIG_PATH`: Path to `service-account.json` for FCM notifications.
- `WEBAUTHN_RP_ID`: Your domain for Passkey support (e.g., `localhost`).

## Useful Gradle Tasks
- `./gradlew build` – clean build and compile all 9 modules.
- `./gradlew test` – runs the test suite (includes **SSRF Hardening**, **Auth**, **SecurityConfig**, and background-job tests).
- `./gradlew ktlintCheck detekt` – lint and static analysis (see [Code Style](../CONTRIBUTING.md#code-style) in CONTRIBUTING.md).
- `./gradlew flywayMigrate` – manually trigger database schema updates.

## API & Debugging
- **Swagger UI (local):** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Swagger UI (live):** [https://coupons-api.thanh0x.com/swagger-ui/index.html](https://coupons-api.thanh0x.com/swagger-ui/index.html)
- **OpenAPI JSON:** `/v3/api-docs`
- Every endpoint's auth requirement is documented via a `bearerAuth` security scheme - look for the lock icon in Swagger UI, or use the Authorize button to test protected endpoints directly.
- See [business-logic.md](business-logic.md) for how requests flow between modules.
- **Audit Logs:** Check the `scraping_task_logs` table in the database to debug asynchronous background scraping tasks.

## Troubleshooting
- **SSRF Blocked:** If you see "SSRF Blocked" in logs, the URL you submitted resolves to a private IP or is not in the `ALLOWED_DOMAINS` list in `UrlValidator`.
- **Handshake Expired:** Passkey/WebAuthn challenges expire after 5 minutes in Redis.

## Contributing
- Follow `CONTRIBUTING.md`.
- Ensure all tests pass (`./gradlew test`) before committing new logic.

You're ready to build! Spin up Docker, run the API, and start discovering deals.
