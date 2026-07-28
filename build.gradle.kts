plugins {
    id("org.springframework.boot") version "3.5.7" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    kotlin("jvm") version "1.9.21" apply false
    kotlin("plugin.spring") version "1.9.21" apply false
    kotlin("plugin.jpa") version "1.9.21" apply false
    kotlin("plugin.allopen") version "1.9.21" apply false
    kotlin("plugin.noarg") version "1.9.21" apply false
    java
}

allprojects {
    group = "com.thanh0x.coursedeal"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Simplified task - delegates to a shell script (much less boilerplate)
tasks.register<Exec>("bootRunLocal") {
    group = "application"
    description = "Runs both API service and Crawler service in parallel with local profile"
    commandLine("bash", "scripts/bootRunLocal.sh")
}
