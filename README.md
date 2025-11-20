# Spring Boot: 100% Off Udemy Coupon Server

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.1-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

<a href="https://coupons.thanh0x.com/"> <img alt="Spring Boot Coupon Server" src="https://raw.githubusercontent.com/datacenter0x/static/main/uPic/RqCLnh.png"></a>

The Spring Boot Coupon Server is a robust application designed to crawl 100% off coupons from various websites and validate them using the official Udemy API. It provides several API endpoints for fetching, searching, and filtering these free coupons, with built-in authentication and authorization.
## Features

- **Coupon Crawling**: Automatically fetch coupons from multiple sources.
- **Coupon Validation**: Validate coupons using the Udemy API then filter 100% off coupon only
- **Search Functionality**: Search for coupons by query.
- **Filter Functionality**: Filter coupons based on various criteria.
- **Authentication & Authorization**: Secure access to API endpoints.

## Prerequisites
- [Java 17](https://jdk.java.net/17/) or higher (JDK)
- with [Docker](https://www.docker.com/) (for MySQL container)
- or [Docker Compose](https://docs.docker.com/compose/)

## Getting Started

### Clone the Repository

```shell
git clone https://github.com/huythanh0x/udemy_coupon_server_java_spring
cd udemy_coupon_server_java_spring
```

### Start the Server
1. Docker Compose - recommended for parity with production:

```shell
docker compose up
```

2. Local development (run app from source, MySQL via local compose):

```shell
docker compose -f docker-compose.local.yml up -d mysql
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Database migrations

- Schema changes and seed data are managed by [Flyway](https://flywaydb.org/).
- Migration scripts live under `src/main/resources/db/migration` (e.g., `V1__init_schema.sql`).
- When the Spring Boot app starts it automatically runs pending migrations; no manual SQL is required.
- For local verification you can run `./gradlew flywayMigrate` once your MySQL instance is up.

## API Documentation
For detailed API documentation, please refer to the [GitHub Wiki](DOCUMENTS.md).

## Contributing
We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for more details.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for details.

## Contact
For any inquiries or issues, please open an issue on GitHub or contact us at <a href="mailto:huythanh0x@gmail.com">huythanh0x@gmail.com</a>