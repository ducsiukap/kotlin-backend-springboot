# **Setup _API Gateway_**

## **`1.` Init project**

Init project at [Spring Initializr](https://start.spring.io/)

## **`2.` Dependencies**: [build.gradle.kts](/codes/api-gateway/build.gradle.kts)

```kotlin
extra["springCloudVersion"] = "2025.1.0"
val jjwtVersion = "0.13.0"

dependencies {
    // --- Gateway ---
    // Sử sụng Gateway reactive -> dùng Netty thay Tomcat
    // -> KHÔNG có spring-boot-starter-web
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    // --- Eureka client ---
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // --- Redis reactive -> rate limiter ---
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // --- Cloud Circuit Breaker - Resilience4J ---
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

    // --- Actuator ---
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // --- Spring Security WebFlux ---
    implementation("org.springframework.boot:spring-boot-starter-security")
    // --- JJWT ---
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // Kotlin
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}
```

## **`3.` Configuration**
