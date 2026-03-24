# **Setup _API Gateway_**

## **`1.` Init project**

Init project at [Spring Initializr](https://start.spring.io/)

**Dependencies**: [build.gradle.kts](/codes/microservices/api-gateway/build.gradle.kts)

- **Spring Cloud Reactive Gateway**: `org.springframework.cloud:spring-cloud-starter-gateway-server-webflux`
- **Config Client**: `org.springframework.cloud:spring-cloud-starter-config`
- **Eureka Client**: `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`
- **Actuator**: `org.springframework.boot:spring-boot-starter-actuator`
- **Auth**: `jjwt` for demo

  ```kotlin
  val jjwtVersion = "0.13.0"

  dependencies {
      // ...

      // jjwt
      implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
      runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
      runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")
  }
  ```

- **Circuit Breaker**: `org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j`
- **Rate Limiting**: `org.springframework.boot:spring-boot-starter-data-redis-reactive`

---

## **`2.` Triển khai**

### **`2.1.` Gateway Config**: [config-repo/api-gateway/api-gateway.yml](/codes/microservices/config-repo/api-gateway/api-gateway.yml)

```yml
# config-repo/api-gateway.yml
server:
  port: 8080

spring:
  cloud:
    gateway:
      server:
        webflux:
          # tự động tạo route từ Eureka
          discovery:
            locator:
              # nếu true -> Gateway tự tạo route dựa trên service name từ Eureka
              #     ex: http://gateway/product-service/**
              # false -> tắt auto-routing -> Sẽ tự định nghĩa route thủ công (control tốt hơn)
              enabled: false

          # CORS
          globalcors:
            cors-configurations:
              "[/**]":
                allowed-origins: "*"
                allowed-methods: "GET, POST, PUT, DELETE, OPTIONS"
                allowed-headers: "*"
                # Không cho gửi cookie / credentials
                # nếu dùng login session thì phải set true
                allow-credentials: false
          default-filters:
            # fix CORS duplicate header khi:
            #   + Gateway thêm header
            #   + Service downstream cũng thêm header
            # -> bị duplicate -> browser chặn
            # DedupeResponseHeader -> loại header trùng
            - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin

#  /* ========================================
#  * Đăng ký gateway vào Eureka server
#  ======================================== */
# -> cho phép:
#   + gọi service qua tên
#   + load balancing khi có nhiều instance
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
  instance:
    prefer-ip-address: true

#  /* ========================================
#  * JWT
#  ======================================== */
app:
  jwt:
    secret: ${JWT_SECRET}

#  /* ========================================
#  * ACTUATOR
#  ======================================== */
management:
  endpoints:
    web:
      exposure:
        include: health, info, gateway, metrics
  endpoint:
    gateway:
      # Cho phép debug Gateway
      # Xem route, refresh route, debug filter
      enabled: true
```

### **`2.2.` _Filter_**

- `GlobalFilter` ( + `Ordered`) -> áp dụng cho mọi request đi qua Gateway Routing, ngoại trừ:
  - Actuator (có thể bypass)
  - Static Resource
  - ...

  Details: [api_gateway/filter/LoggingFilter.kt](/codes/microservices/api-gateway/src/main/kotlin/com/vduczz/api_gateway/filter/LoggingFilter.kt)

- `AbstractGatewayFilterFactory` / `GatewayFilter` -> custom filter theo route

  Details:
  - Filter: [api_gateway/filter/AuthFilter.kt](/codes/microservices/api-gateway/src/main/kotlin/com/vduczz/api_gateway/filter/AuthFilter.kt)
  - JwtUtil: [api_gateway/util/JwtUlti.kt](/codes/microservices/api-gateway/src/main/kotlin/com/vduczz/api_gateway/util/JwtUlti.kt)

### **`2.3.` Circuit Breaker**: [config-repo/api-gateway/api-gateway.yml](/codes/microservices/config-repo/api-gateway/api-gateway.yml)

```yml
#  /* ========================================
#  * RESILIENCE
#  ======================================== */
resilience4j:
  circuitbreaker:
    instances:
      order-service-cb: # cb for order-service
        failure-rate-threshold: 50
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
      payment-service-cb: # cb for payment
        failure-rate-threshold: 50
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 15s

  timelimiter:
    instances:
      order-service-cb:
        timeout-duration: 5s # timeout after 5s
      payment-service-cb:
        timeout-duration: 8s
```

### **`2.4.` Route Config**

#### **Config by `code`**: [api_gateway/config/RouteConfig.kt](/codes/microservices/api-gateway/src/main/kotlin/com/vduczz/api_gateway/config/RouteConfig.kt)

#### **Using `.yml`**: [config-repo/api-gateway/api-gateway.yml](/codes/microservices/config-repo/api-gateway/api-gateway.yml)

```yml
# example
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: order-route
              uri: lb://order-service
              predicates:
                - Path=/api/orders/**
              filters:
                # AuthFilter
                - AuthFilter
                # -----------------------
                # Circuit Breaker
                - name: CircuitBreaker
                args:
                    name: order-service-cb
                    # khi OPEN, forward tới fallback endpoint
                    fallbackUri: forward:/fallback/order-service
```

- Fallback endpoint: [api_gateway/controller/FallbackController.kt](/codes/microservices/api-gateway/src/main/kotlin/com/vduczz/api_gateway/controller/FallbackController.kt) (fallbackUri forward)

### **`2.5.` Retry**

- Notes: **Retry** chỉ nên dùng:
  - cho route hứng `GET` Request.
  - retry for **temporary error** (`timeout`, `5xx`)

```yml
# -------------------------
# Retry
routes:
  filters:
    - name: Retry
    args:
        retries: 3
        statuses: BAD_GATEWAY, SERVICE_UNAVAILABLE # retry only with errorcode: 502, 503
        methods: GET # only GET request can be retried
        backoff:
        firstBackoff: 100ms # lần 1, chờ 100ms
        maxBackoff: 1000ms
        factor: 2
        basedOnPreviousValue: false
```

### **`2.6.` Rate Limiter**

**Yêu cầu**: Chạy `Redis`.

```yml
routes:
  filters:
    # --------------------------
    # Rate Limiter
    - name: RequestRateLimiter
    args:
        redis-rate-limiter.replenishRate: 50 # số token được nạp lại / s (số request thêm mới / s)
        redis-rate-limiter.burstCapacity: 100 # số token tối đa trong bucket (số request tối đa trong 1 khoảng thời gian)
        # redis-rate-limiter.requestedTokens: 1 # số token / request

        # Public route → rate limit theo IP
        key-resolver: "#{@ipKeyResolver}"
        # Protected route -> rate limit by userId
        # key-resolver: "#{@userKeyResolver}"
```

**Key Resolver**: [api_gateway/config/RateLimitConfig.kt](/codes/microservices/api-gateway/src/main/kotlin/com/vduczz/api_gateway/config/RateLimitConfig.kt)

### **`2.7.` Có nên _CB_, _Retry_, _Rate Limiter_ cho mọi _`Routes`_?**

|             | CB                                                                                                                                                            | Retry                                                                                                 |                                                    RateLimiter                                                     |
| :---------: | :------------------------------------------------------------------------------------------------------------------------------------------------------------ | :---------------------------------------------------------------------------------------------------- | :----------------------------------------------------------------------------------------------------------------: |
| **Usecase** | - **CẦN** cho service có thể `fail`<br/>ex: payment, external API, recommendation, ...<br/>- **KHÔNG CẦN** cho service internal ổn định (user, product basic) | - `GET` request / temporary error (timeout, 5xx)<br/>- **KHÔNG NÊN** dùng cho `POST`, `PUT`, `DELETE` | - **NÊN** dùng cho: public API, auth, search, ...<br/>- **KHÔNG CẦN** cho internal `service-to-service`, admin API |
