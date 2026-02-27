plugins {
    kotlin("jvm") version "2.2.21"
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"

    // Spring Data JPA plugins
    // all-open -> open toàn bộ class
    kotlin("plugin.spring") version "2.2.21"
    // no-arg constructor
    kotlin("plugin.jpa") version "2.2.21"
}

group = "com.vduczz"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Spring Data JPA dependencies
    // Spring Data JPA + Hibernate
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // spring-boot-starter-data-jpa
    // MySQL
    implementation("com.mysql:mysql-connector-j")
    // Flyway - db migration
    implementation("org.springframework.boot:spring-boot-starter-flyway") // spring-boot-starter-flyway
    implementation("org.flywaydb:flyway-mysql") // mysql
    //    implementation("org.flywaydb:flyway-core") // Spring-> cần thêm flyway-core
    //
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
