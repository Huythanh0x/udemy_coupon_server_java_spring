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
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.json:json:20231013")
    api("jakarta.validation:jakarta.validation-api:3.0.2")
}

