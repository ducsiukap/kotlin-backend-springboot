# _**Synchronous**_ communication

## **1. Bản chất**

Bản chất của _**Synchronous**_ là cơ chế **Blocking**: Khi **`Service A`** gửi _HTTP Request_ (**REST**, **gRPC**,..) tới **`Service B`**

- **_A_** mở một kết nối mạng - **_`Connection`_** - tới **_B_**.

  Khi này, trong khi chờ B phản hồi, thread của A bị **_`block`_**, **KHÔNG** thể thực hiện task or phục vụ client khác.

- **_B_** xử lý xong, trả `HTTP Response` về cho **_A_**
- _**A**_ nhận được phản hồi từ B, _giải phóng Thread_ và tiếp tục thực hiện task tiếp theo.

**_`Advantages`_**:

- **Real-time Feedback** - Phản hồi tức thì: _**A**_ biết được kết quả thực hiện từ **_B_** là _`thành công`_ hay _`thất bại`_ để phản hồi chính xác về **client**.
- **Simple Flow** - Flow dễ hiểu: Luồng code **_tuần tự_** từ trên xuống dưới, **dễ hiểu**, **dễ debug**.
- **Strong Consistency** - Tính **Nhất quán** cao: phù hợp với các nghiệp vụ đòi hỏi sự **_CHÍNH XÁC TUYỆT ĐỐI_** ngay lập tức.

**_`Disadvantages`_**:

- **Temporal Coupling** - Ràng buộc về mặt thời gian: Để _**A**_ có thể phục vụ client, **BẮT BUỘC _B_ PHẢI SỐNG**. Khi này, **_nếu B gặp lỗi, A cũng văng lỗi theo_**.
- **Resource Exhaustion** - Cạn kiệt tài nguyên: **High Latency** của B (do lag, lỗi, ...) dẫn tới Thread của A bị treo.

  Khi này, nếu có nhiều kết nối tới B dẫn tới số lượng Thread bị treo của A tăng đáng kể, và sập nếu A cạn Thread.

- **Cascading Failures** - Hiệu ứng chết chùm: do **Temporary Coupling** dẫn tới việc B gặp lỗi làm A ngừng phục vụ, từ đó các Service phụ thuộc A cũng ngừng phục vụ theo.

  **Hiệu ứng `Domino`**: cả hệ thống sập hoàn toàn vì chỉ một **_microservice B_** bị lag/sập.

## **2. _Implementations_**

Dể giao tiếp giữa các service, **Spring Boot** có:

- **RestTemplate**: là thư viện cũ, phải viết code rườm rà và lặp lại (**_boilerplate_**)
- **WebClient**: là thư viện hệ **Reactive** (`non-blocking`), xịn, nhanh nhưng dự án cần viết theo chuẩn **Reactive** từ đầu.
- **Spring Cloud OpenFeign**: là **Declaretive REST Client**, chỉ cần khai báo `interface` và sử dụng _Annotation_ tương tự JpaRepository, Controller, ... Spring tự động sinh code (**Proxy**) ở dưới để gọi APIs.

#### Triển khai sử dụng **_OpenFeign_**

1. Dependencies

   ```kotlin
   // spring cloud version
   extra["springCloudVersion"] = "2025.1.1"
   // openfeign
   dependencies {
       implementation("org.springframework  .cloud:spring-cloud-starter-openfeign")
   }
   // Spring Cloud BOM
   //  -> openfeign version
   dependencyManagement {
       imports {
           mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
       }
   }

   ```

2. Enable FeignClient: use `@EnableFeignClients` trong [**_user-service_**/src/.../**UserServiceApplication.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/UserServiceApplication.java)
3. OpenFeign configuration:
   - communication config: [**_user-service_**/src/main/resources/**application.yaml**](/codes/monorepo-microservice-example/user-service/src/main/resources/application.yaml)
   - `logging` + `interceptor` (add header for every requests) config: [**_user-service_**/src/.../infrastructure/config/feign/**FeignConfig.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/config/feign/FeignConfig.java)
   - requires: enable logging: [**_user-service_**/src/main/resources/**application.yaml**](/codes/monorepo-microservice-example/user-service/src/main/resources/application.yaml)
   - `error-decoder`: [**_user-service_**/src/.../infrastructure/config/exception/**FeignErrorDecoder.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/config/exception/FeignErrorDecoder.java)
4. Fallback: tránh **Cascading Failures**
   `// Hiện nay, nên dùng Circuit Breaker riêng: Resilience4j`

   ```java
   // Nếu vẫn muốn dùng fallback của @FeignClient
   // Phải bật Circuit Breaker trong application.yaml

   // ============================================================
   // Fallback class -> không biết thông tin về lỗi
   @Component
    public class OrderClientFallback implements OrderClient {
        @Override
        public OrderResponse getOrder(String id) {
            // logic..
            // trả về default OrderResponse
            // or, trả về null
            return null;
        }
    }

    // ============================================================
    // FallbackFactory -> giúp bổ sung thông tin về lỗi
    @Component
    public class OrderFallbackFactory implements FallbackFactory<OrderClient> {

        @Override
        // Khi có lỗi, hàm create được gọi,
        // OpenFeign sẽ truyền cause vào
        public OrderClient create(Throwable cause)
        {
            // ------------------------------------------------------------
            // + throw lỗi
            //      return id -> {
            //          throw new RuntimeException("Order service down");
            //      };

            // ------------------------------------------------------------
            // hoặc
            // + trả về OrderResponse mặc đinh
            return new OrderClient() {
                @Override
                public OrderResponse getOrder(String id) {
                    // quan trọng nhất: logging
                    System.out.println("Feign error: " + cause);
                    return new OrderResponse(id, "fallback", 0);
                }};}

            // ------------------------------------------------------------
            // + or xử lý gì đó hữu ích hơn
            //      if (cause instanceof FeignException.NotFound) {
            //          return null;
            //      }
            //      if (cause instanceof RetryableException) {
            //          throw new ServiceUnavailableException();
            //      }
    }
   ```

5. Triển khai `FeignClient`:
   - dto: [**_user-service_**/src/.../infrastructure/client/dto/**WelcomeMailRequest.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/client/dto/WelcomeMailRequest.java)
   - NotificationClient: [**_user-service_**/src/.../infrastructure/client/**NotificationClient.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/client/NotificationClient.java)

6. Service gọi `FeignClient`: [**_user-service_**/src/.../application/service/impl/**AuthServiceImpl.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/application/service/impl/AuthServiceImpl.java)
