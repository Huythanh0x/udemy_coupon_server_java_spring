plugins {
    `java-library`
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(project(":modules:coupon-domain"))
    api(project(":modules:coupon-infrastructure"))
    implementation(libs.spring.boot.starter.data.redis)
    api(libs.jsoup)
    api(libs.json.org)
    implementation(libs.spring.boot.starter)
    api(libs.jobrunr.spring.boot.starter)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
}
