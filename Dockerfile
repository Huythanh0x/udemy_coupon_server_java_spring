## Build stage
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Build argument to specify which service to build
# Default: coupon-api-service (override via docker-compose build args)
# Valid values: coupon-api-service, coupon-crawler-service
ARG SERVICE_NAME=coupon-api-service

COPY gradlew .
COPY gradle gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY modules modules/

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew --no-daemon :modules:${SERVICE_NAME}:dependencies

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew --no-daemon :modules:${SERVICE_NAME}:bootJar -x test

## Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Re-declare ARG for use in this stage (inherits value from build stage)
ARG SERVICE_NAME=coupon-api-service

COPY --from=builder /app/modules/${SERVICE_NAME}/build/libs/*.jar app.jar

RUN useradd -m -u 1001 appuser && chown -R appuser:appuser /app
USER appuser

# Expose port (actual port mapping is done in docker-compose)
EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
