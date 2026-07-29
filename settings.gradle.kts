plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "course-deal-server"
include(
    "modules:coupon-common",
    "modules:coupon-domain",
    "modules:coupon-infrastructure",
    "modules:notification-service",
    "modules:course-scraper",
    "modules:course-external-api",
    "modules:identity-service",
    "modules:coupon-api-service",
    "modules:coupon-crawler-service"
)
