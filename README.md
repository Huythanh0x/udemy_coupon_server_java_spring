# Spring Boot: Course Deal Server (Kotlin)

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

The Course Deal Server is a modern, asynchronous application designed to crawl 100% off coupons from various websites and validate them. It provides a robust REST API for fetching, searching, and filtering free coupons, featuring password-less authentication and automated background processing.

## Features

- **Automated Crawling**: Discovers course deals from multiple aggregator sources.
- **Asynchronous Scraping**: Non-blocking background validation engine powered by **JobRunr**.
- **Modern Authentication**: Secure, biometric-ready auth supporting **Google**, **Apple**, and **Passkeys (WebAuthn)**.
- **Targeted Notifications**: Personalized **FCM push notifications** based on user-defined keywords and categories.
- **SSRF Hardened**: Strict URL validation to protect infrastructure.
- **Search & Filter**: Powerful querying capabilities for discovering the best deals.

## Architecture
- `modules/coupon-domain`: Pure data layer containing JPA entities, repositories, and DTOs.
- `modules/coupon-infrastructure`: Shared technical services including Redis, the Scraper engine, and FCM.
- `modules/coupon-api-service`: REST API exposed on port 8080.
- `modules/coupon-crawler-service`: Discovery workers and background job server (port 8081).

## Prerequisites
- [Java 17](https://jdk.java.net/17/) or higher (JDK)
- with [Docker](https://www.docker.com/) (for MySQL container)
- or [Docker Compose](https://docs.docker.com/compose/)

## Getting Started

### Clone the Repository

```shell
git clone https://github.com/huythanh0x/course-deal-server
cd course-deal-server
```

### Start the Services
1. Full stack via Docker Compose (published images - CI parity) - recommended:

```shell
docker compose -f docker-compose.prod.yml up
```

This pulls the API/crawler images built by GitHub Actions (and starts MySQL/Redis plus the observability stack).

2. Local development (run services from source, MySQL via local compose):

```shell
docker compose -f docker-compose.local.yml up -d
./gradlew :modules:coupon-api-service:bootRun --args='--spring.profiles.active=local'
# optional crawler worker
./gradlew :modules:coupon-crawler-service:bootRun --args='--spring.profiles.active=local'
```

## Database migrations

- Schema changes and seed data are managed by [Flyway](https://flywaydb.org/).
- Migration scripts live under `modules/coupon-domain/src/main/resources/db/migration` (e.g., `V1__init_schema.sql`).
- When the Spring Boot app starts it automatically runs pending migrations; no manual SQL is required.
- For local verification you can run `./gradlew :modules:coupon-api-service:flywayMigrate` (or the crawler equivalent) once MySQL is up.

## API Documentation
Once the server is running, navigate to [Swagger UI](http://localhost:8080/swagger-ui/index.html) for interactive docs or fetch the OpenAPI JSON at `/v3/api-docs`. See `docs/getting-started.md` for setup instructions and `docs/business-logic.md` for flow details.

You can also view the live Swagger API documentation at [swagger-ui/index.html](https://coupons-api.thanh0x.com/swagger-ui/index.html).

## Contributing
We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for more details.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for details.

## Contact
For any inquiries or issues, please open an issue on GitHub or contact us at <a href="mailto:huythanh0x@gmail.com">huythanh0x@gmail.com</a>