pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.21"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "training_thanhvh_java_spring_jwt_jpa"
include(
    "modules:coupon-domain",
    "modules:coupon-api-service",
    "modules:coupon-crawler-service"
)
