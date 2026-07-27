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
    // For @Configuration, @Bean, etc
    implementation("org.springframework.boot:spring-boot-starter")
}
