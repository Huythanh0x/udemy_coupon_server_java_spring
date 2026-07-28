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
}
