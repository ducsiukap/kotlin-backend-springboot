# **Circuit Breaker: `spring-cloud-starter-circuitbreaker-resilience4j` + `OpenFeign`**

**Feign** và **Spring Cloud CB** được thiết kế trong **_cùng một hệ sinh thái_** — `Spring Cloud`. **Feign** đã có sẵn `hook` để **tích hợp CB**, chỉ cần bật `enabled: true` là Feign tự **wrap mọi call** qua `CircuitBreakerFactory`.

## **`1.` Dependencies**

```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
implementation("org.springframework.boot:spring-boot-starter-actuator") // actuator (có thể xem thông số CB qua endpoint)
implementation("org.springframework.cloud:spring-cloud-starter-openfeign") // OpenFeign
```

## **`2.` Configuration**

#### Enable `Feign`

```java
// file chạy chường trình, MainApplication.kt
@SpringBootApplication
@EnableFeignClients // enable feign
class UserServiceApplication
```

#### `application.yml`

```yml
spring:
cloud:
  openfeign:
  circuitbreaker:
    enabled: true # bắt buộc — không có dòng này fallback không chạy

resilience4j:
circuitbreaker:
  instances:
  notification-service: # id -> phải khớp với name trong @FeignClient
    registerHealthIndicator: true # cho phép CB xuất trạng thái qua Actuatir // endpoint: /actuator/health
    slidingWindowSize: 10
    minimumNumberOfCalls: 5
    failureRateThreshold: 50
    slowCallRateThreshold: 50 # ngoài error, slow request cũng tính để chuyển trạng thái OPEN
    slowCallDurationThreshold: 3s # timeout
    waitDurationInOpenState: 10s # OPEN trong 10s
    automaticTransitionFromOpenToHalfOpenEnabled: true # sau khi waitDurationInOpenState hết, tự chuyển từ OPEN -> HALF_OPEN
    permittedNumberOfCallsInHalfOpenState: 3 # ở HALF_OPEN, lấy 3 request để test

# Actuator
management:
endpoints:
  web:
  exposure:
    include: health, circuitbreakers, circuitbreakerevents
health:
  circuitbreakers:
  enabled: true
```

## **`3.` Usage**

#### **`@FeignClient` + `fallback`**

```kotlin
@FeignClient(
    name = "notification-service", // id
    fallback = NotificationFallback::class // fallback class
)
interface NotificationClient { //
    @PostMapping("/api/v1/emails/welcome")
    fun sendWelcomeEmail(@RequestBody request: WelcomeEmailRequest): ResponseEntity<Void>

    @PostMapping("/api/v1/emails/reset-password")
    fun sendResetPasswordEmail(@RequestBody request: ResetPasswordRequest): ResponseEntity<Void>
}

@Component
// Fallback Class -> fallback cho trường hợp call thất bại
// implements interface dùng nó làm fallback
class NotificationFallback : NotificationClient {

    private val log = LoggerFactory.getLogger(this::class.java)


    // impl cho từng method
    // => default behavior cho từng method

    override fun sendWelcomeEmail(request: WelcomeEmailRequest): ResponseEntity<Void> {
        log.warn("CB open — skipping welcome email for ${request.email}")
        // Không throw exception — trả về graceful response
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }

    override fun sendResetPasswordEmail(request: ResetPasswordRequest): ResponseEntity<Void> {
        log.error("CB open — reset password email FAILED for ${request.email}")
        // Email này quan trọng hơn — có thể throw để caller biết
        throw ServiceUnavailableException("Notification service unavailable")
    }
}
```

#### **`fallback` có _exceptions_ => `FallbackFactory<T>`**

```kotlin
@FeignClient(
    name = "notification-service",
    fallbackFactory = NotificationFallbackFactory::class  // dùng factory thay fallback
)
interface NotificationClient {
    @PostMapping("/api/v1/emails/welcome")
    fun sendWelcomeEmail(@RequestBody request: WelcomeEmailRequest): ResponseEntity<Void>
}

@Component
class NotificationFallbackFactory : FallbackFactory<NotificationClient> {

    private val log = LoggerFactory.getLogger(this::class.java)

    // Fallback<T> -> return T
    override fun create(cause: Throwable): NotificationClient {
        return object : NotificationClient {
            // implementation cho từng methods của client
            override fun sendWelcomeEmail(request: WelcomeEmailRequest): ResponseEntity<Void> {
                // Giờ biết chính xác lỗi gì
                return when (cause) {
                    // OPEN state
                    is CallNotPermittedException -> {
                        log.warn("CB OPEN — notification service down")
                        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
                    }
                    //
                    is FeignException.ServiceUnavailable -> {
                        log.error("503 from notification service")
                        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
                    }
                    // Timeout exceotion
                    is TimeoutException -> {
                        log.error("Timeout calling notification service")
                        ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build()
                    }
                    else -> {
                        log.error("Unknown error: ${cause.message}")
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                    }
                }
            }
        }
    }
}
```

## **`4.` Hanlde `FeignException`**

Mặc định `Feign` **không coi** HTTP `4xx`/`5xx` là `exception` để CB đếm — nó **wrap vào `FeignException`** và **`trả về bình thường`** => **_CB không trip dù service trả về 500 liên tục_**

```java
// Cần custom ErrorDecoder để CB đếm đúng
@Component
class FeignErrorDecoder : ErrorDecoder {
    override fun decode(methodKey: String, response: Response): Exception {
        return when (response.status()) {
            503 -> ServiceUnavailableException("Service unavailable") // CB đếm cái này
            504 -> GatewayTimeoutException("Gateway timeout")         // và cái này
            in 500..599 -> ServerException("Server error ${response.status()}")
            else -> Default().decode(methodKey, response)             // 4xx không đếm
        }
    }
}
```

#### Có thể khai báo Exception nào là `lỗi` để CB **đếm**, exception nào được **bỏ qua**:

```yml
# Khai báo exception nào CB sẽ đếm là lỗi
resilience4j:
circuitbreaker:
  instances:
  notification-service:
    recordExceptions: # coi là lỗi -> tăng failure-rate
      - feign.FeignException.ServiceUnavailable
      - java.util.concurrent.TimeoutException
      - com.example.exception.ServerException
    ignoreExceptions: # bỏ qua, không tăng failure-rate
      - feign.FeignException.BadRequest # 400 — lỗi do mình, không đếm
      - feign.FeignException.NotFound # 404 — không đếm
```
