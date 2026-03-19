# **`resilience4j-spring-boot3` - _Native_ implementations for `Resilience4j`**

## **1. Native Spring Boot configuration for `Resilience4j`**

#### **`1.1.` Nhược điểm của `spring-cloud-starter-circuitbreaker-resilience4j` + `OpenFeign`**

Khi này, `@FeignClient` phải **làm quá nhiều** việc:

- Làm **HTTP Client**, giao tiếp với Service khác.
- Bọc, triển khai **Circuit Breaker** và quyết định CB.
- Vẫn không có các **Annotations** quan trọng của `Resilience4j`.

#### **`1.2.` `resilience4j-spring-boot3`**

Là **Native SpringBoot autoconfiguration** của `Resilience4j`, mang lại toàn bộ các tính năng của nó:

- **Full pattern stack**, tất cả đều `composable` (có thể kết hợp): `@CircuitBreaker`, `@Retry`, `@Bulkhead`, `@RateLimiter`, `@TimeLimiter`, ...
- Không phụ thuộc vào `OpenFeign`, nó có thể bảo vệ mọi thứ, từ **get from `Redis`**, **upload to `S3`**, ...
- **Fully Configuration** trong `application.yaml`, không cần code `@Bean` để config.
- **Metrics** đầy đủ cho Prometheus/Grafana tự động, không cần config thêm

**Nhược điểm**:

- `fallback` fallback là **string `tên method`**, compiler không kiểm tra.
- Nếu dùng với `OpenFeign`, cần **tắt Feign CB** để tránh **double CB**.
- **Annotations `order` quan trọng**, sai thứ tự dẫn tới behavior thay đổi.

---

## **`2.` Dependencies**

```kotlin
implementation("io.github.resilience4j:resilience4j-spring-boot3")
// BẮT BUỘC PHẢI CÓ: AOP để các Annotation (@CircuitBreaker, @Retry) hoạt động được
implementation("org.springframework.boot:spring-boot-starter-aop")
implementation("org.springframework.boot:spring-boot-starter-actuator") // Actuator
```

## **`3.` Configuration**

### `Feign` timeout vs `TimeLimiter`

- `Feign timeout`: quản lý giao tiếp ở `network layer`, đặt timeout cho quá trình **gửi request** và **nhận response**
  - **Connection Timeout**: thời gian tối đa để `handshake`. -> `ConnectTimeoutException`
  - **Read Timeout**: Thời gian tối đa chờ phản hồi từ phía client. -> `SocketTimeoutException`
- `@TimeLimiter`: không liên quan gì tới HTTP, được thiết kế cho tầng ứng dụng. `@TimeLimiter` đặt **`timeout` cho toàn bộ hàm**, wrap phản hồi vào `CompletableFuture` và có khả năng **cancel future** sau `timeout`.  
  **Note**: `@TimeLimiter` thực chất:
  - Tạo luông phụ chạy song song để tính toán, gọi client.

  Khi timeout của TimeLimiter xảy ra:
  - ném `TimeoutException` ra main thread
  - gọi `future.cancel(true)` ở luồng phụ đang gọi FeignClient, thực chất là lệnh `thread.interrupt()`.

  Bản chất `OpenFeign` dùng cơ chế **Blocking I/O** nên không thể gọi `thread.interrupt()` nếu HTTP call đang diễn ra -> mặc định chạy tới khi client phản hồi / connection timeout -> `TimeLimiter` không có khả năng dừng HTTP call đang chạy bên trong.

  `@TimeLimiter` chỉ có tác dụng khi bọc bên ngoài hàm có khả năng async như các thư viện NIO/WebClient, WebFlux, .. Khi này, `CompletableFuture` mới thực sự có tác dụng khi gọi hàm `cancel()`

  **Nếu kết hợp `@TimeLimiter` + `OpenFeign`**, phải đảm bảo `read-timeout` + `connection-timeot` < `timeout` của TimeLimiter. Thường thì chỉ cần **timeout của OpenFeign** là **đủ**
