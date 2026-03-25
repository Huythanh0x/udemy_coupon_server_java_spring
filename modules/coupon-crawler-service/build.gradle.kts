plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
    kotlin("jvm")
}

import org.gradle.api.tasks.JavaExec

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Load .env file for bootRun tasks to ensure environment variables are available
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    doFirst {
        val envFile = file("${rootProject.projectDir}/.env")
        if (envFile.exists()) {
            println("🔐 Loading environment variables from .env")
            envFile.readLines().forEach { line ->
                if (line.isNotBlank() && !line.trimStart().startsWith("#") && line.contains("=")) {
                    val (key, value) = line.split("=", limit = 2)
                    val envKey = key.trim()
                    var envValue = value.trim()
                    // Remove quotes if present
                    if ((envValue.startsWith("\"") && envValue.endsWith("\"")) ||
                        (envValue.startsWith("'") && envValue.endsWith("'"))) {
                        envValue = envValue.substring(1, envValue.length - 1)
                    }
                    // Only set if not already in environment
                    if (System.getenv(envKey) == null) {
                        environment(envKey, envValue)
                    }
                }
            }
        } else {
            println("ℹ️  .env file not found. Using default configuration values.")
        }
    }
}

dependencies {
    implementation(project(":modules:coupon-domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("org.flywaydb:flyway-mysql")
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("org.json:json:20231013")
}

// Run extractor against a single URL without starting the web server.
tasks.register<JavaExec>("debugExtractor") {
    group = "application"
    description = "Runs UdemyCouponCourseExtractor for one URL (via --url or UDEMY_DEBUG_URL)"

    // Ensure logback loads our crawler logback config even without Spring Boot.
    jvmArgs("-Dlogback.configurationFile=classpath:logback-spring.xml")

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.huythanh0x.udemycoupons.tools.ExtractorDebugMain")

    // URL is read by the main() method from:
    // - command line args: --url=<couponUrl>
    // - env var: UDEMY_DEBUG_URL
    // - system property: udemy.debugUrl
}

