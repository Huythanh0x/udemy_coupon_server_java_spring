plugins {
    `java-library`
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.7")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(project(":modules:coupon-domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.jsoup:jsoup:1.15.4")
    api("org.json:json:20231013")
    implementation("org.slf4j:slf4j-api")
    implementation("com.google.firebase:firebase-admin:9.3.0")
    implementation("org.springframework.boot:spring-boot-starter")
    api("org.jobrunr:jobrunr-spring-boot-3-starter:8.7.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
