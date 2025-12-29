import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
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
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")
    implementation("org.flywaydb:flyway-mysql")
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
}

