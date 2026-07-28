plugins {
    `java-library`
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
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
    api(project(":modules:coupon-infrastructure"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.jsoup:jsoup:1.15.4")
    api("org.json:json:20231013")
    implementation("org.springframework.boot:spring-boot-starter")
    api("org.jobrunr:jobrunr-spring-boot-3-starter:8.7.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}
