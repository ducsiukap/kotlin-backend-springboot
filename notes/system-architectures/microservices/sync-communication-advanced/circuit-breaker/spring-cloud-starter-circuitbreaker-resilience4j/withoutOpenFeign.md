# **`spring-cloud-starter-circuitbreaker-resilience4j` _without_ `OpenFeign`**

## **`1.` Dependencies**

```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
implementation("org.springframework.boot:spring-boot-starter-actuator") // actuator (có thể xem thông số CB qua endpoint)
```

---

## **`2.` Configuration**

```kotlin
@Configuration
class CircuitBreakerConfig {

    // Config mặc định áp dụng cho tất cả CB chưa được đặt tên
    @Bean
    fun defaultCustomizer(): Customizer<Resilience4JCircuitBreakerFactory> { // Sử dụng Customizer<>
        return Customizer { factory ->
            // factory.configureDefault
            factory.configureDefault { id -> // id là tên được sinh khi gọi .create(id)
                Resilience4JConfigBuilder(id) // Builder
                    .circuitBreakerConfig( // CB config
                        CircuitBreakerConfig.custom()
                            .slidingWindowSize(10) // 10 request để tính failure rate
                            .minimumNumberOfCalls(5) // số request tối thiểu trước khi CB bắt đầu tính failure rate
                            .failureRateThreshold(50f) // threshold to switch to OPEN
                            .waitDurationInOpenState(Duration.ofSeconds(30)) // khi OPEN, ngừng gọi sang service trong 30s. Sau 30s chuyển HALF_OPEN
                            .permittedNumberOfCallsInHalfOpenState(3) // HALF_OPEN: cho phép 3 request để test
                            .automaticTransitionFromOpenToHalfOpenEnabled(true) // cho phép tự động chuyển OPEN -> HALF_OPEN sau ""withDuration...()""
                            .build()
                    )
                    .timeLimiterConfig( // time limiter
                        TimeLimiterConfig.custom()
                            .timeoutDuration(Duration.ofSeconds(3)) // request >3s -> timeout -> failure
                            .build()
                    )
                    .build()
            }
        }
    }

    // Config riêng cho từng service — override default
    @Bean
    fun paymentCustomizer(): Customizer<Resilience4JCircuitBreakerFactory> {
        return Customizer { factory ->
            // factory.configure
            factory.configure({ builder ->
                builder
                    .circuitBreakerConfig(
                        CircuitBreakerConfig.custom()
                            .slidingWindowSize(20)
                            .failureRateThreshold(30f)   // nhạy hơn vì liên quan tiền
                            .waitDurationInOpenState(Duration.ofSeconds(60))
                            .build()
                    )
                    .timeLimiterConfig(
                        TimeLimiterConfig.custom()
                            .timeoutDuration(Duration.ofSeconds(5))
                            .build()
                    )
            },
             "payment-service"
              // id
              // tên phải khớp khi factory.create(...)
            )
        }
    }
}
```

---

## **`3.` Sử dụng**

```kotlin
@Service
class PaymentService(
    // Inject CircuitBreakerFactory do Spring Cloud tạo
    private val factory: CircuitBreakerFactory<*, *>,
    private val restTemplate: RestTemplate
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun processPayment(request: PaymentRequest): PaymentResponse {

        // tạo CB mới -> cb.create(id)
        val cb = factory.create("payment-service")

        // business logic (ít nhất là hàm call client)
        // chạy trong cb -> cb.run({...})
        return cb.run(
            {
                // call client
                // HTTP request
                restTemplate.postForObject(
                    "http://payment-service/api/payments",
                    request,
                    PaymentResponse::class.java
                )!!
            },
            { ex -> // handle failure error // FALLBACK
                log.warn("Payment CB triggered: ${ex.message}")
                when (ex) {
                    // CB is OPEN
                    is CallNotPermittedException ->
                        PaymentResponse(status = "PENDING", message = "Hệ thống đang bận")
                    // failure do timeout
                    is TimeoutException ->
                        PaymentResponse(status = "TIMEOUT", message = "Request timeout")
                    // các exceptions khác
                    else ->
                        PaymentResponse(status = "ERROR", message = "Lỗi không xác định")
                }
            }
        )
    }
}
```

---

## **`4.` Giảm boilerplate**

```kotlin
// Extension để giảm boilerplate
fun <T> CircuitBreakerFactory<*, *>.execute(
    name: String,
    call: () -> T,
    fallback: (Throwable) -> T
): T = this.create(name).run(call, fallback)

// Hoặc wrap thành base class
abstract class CircuitBreakerService(
    protected val cbFactory: CircuitBreakerFactory<*, *>
) {
    protected fun <T> withCB(
        name: String,
        fallback: (Throwable) -> T,
        call: () -> T
    ): T = cbFactory.create(name).run(call, fallback)
}

// Service kế thừa
@Service
class PaymentService(
    factory: CircuitBreakerFactory<*, *>,
    private val restTemplate: RestTemplate
) : CircuitBreakerService(factory) {

    fun processPayment(request: PaymentRequest) =
        withCB("payment-service", fallback = { PaymentResponse(status = "PENDING") }) {
            restTemplate.postForObject("/api/payments", request, PaymentResponse::class.java)!!
        }

    fun getBalance(userId: String) =
        withCB("payment-service", fallback = { Balance(amount = 0) }) {
            restTemplate.getForObject("/api/balance/$userId", Balance::class.java)!!
        }
}
```

---

## 5. Actuator

`application.yaml`:

```yml
management:
  endpoints:
    web:
      exposure:
        include: health, circuitbreakers, circuitbreakerevents
  health:
    circuitbreakers:
      enabled: true
```

Truy cập:

- `GET /actuator/circuitbreakers`: để ra trang tổng quan CB của service
- `GET /actuator/circuitbreakerevents/payment-service`: để xem chi tiết CB của `payment-service`

---

## **`6.` Advanced: _Retry_, _Bulkhead_, ...**

### **Manual `Retry`**

```kotlin
// Tự implement retry thủ công
fun processWithRetry(request: PaymentRequest): PaymentResponse {
    var lastEx: Exception? = null
    repeat(3) { attempt ->
        try {
            return withCB("payment-service", { PaymentResponse(status = "PENDING") }) {
                restTemplate.postForObject(...)!!
            }
        } catch (ex: Exception) {
            lastEx = ex
            Thread.sleep(500L * (attempt + 1)) // backoff thủ công
        }
    }
    throw lastEx!!
}
```

### **KHÔNG có `Bulkhead`**
