plugins {
    id("org.springframework.boot") version "3.5.7" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    id("io.freefair.lombok") version "9.1.0" apply false
    kotlin("jvm") apply false
    java
}

allprojects {
    group = "com.huythanh0x"
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
