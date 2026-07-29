plugins {
    `java-library`
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencyManagement {
    imports {
        // Overrides the BOM's own kotlin.version (3.5.7 manages 1.9.25) back to this
        // project's actual Kotlin version, otherwise every Kotlin artifact in this module
        // silently resolves to a different version than the rest of the build uses.
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}") {
            bomProperty("kotlin.version", libs.versions.kotlin.get())
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(project(":modules:coupon-domain"))
    api(project(":modules:coupon-common"))
    // PasskeyService stores WebAuthn challenge state via RedisTemplate, whose bean is provided
    // by coupon-infrastructure's RedisConfig (auto-detected at runtime in whichever app hosts
    // this module's controllers).
    api(project(":modules:coupon-infrastructure"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    api(libs.webauthn.server.core)
    api(libs.google.api.client)
    api(libs.jjwt.impl)
    api(libs.jjwt.api)
    api(libs.jjwt.jackson)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    // Annotations only, for @Tag/@Operation/@Parameter on controllers in this module. The actual
    // springdoc/Swagger UI runtime is provided by coupon-api-service, which hosts these controllers.
    compileOnly(libs.swagger.annotations)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.h2)
    testRuntimeOnly(libs.junit.platform.launcher)
}
