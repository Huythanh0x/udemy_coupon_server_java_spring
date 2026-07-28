plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.plugin.spring) apply false
    alias(libs.plugins.kotlin.plugin.jpa) apply false
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
