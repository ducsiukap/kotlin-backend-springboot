# _**DDD**_ in **_Synchronous_** communication

## **`1.` Request/Response _Wrapper_**

**Request** nên được quản lí bởi `/application` hoặc `/infrastructure`

```bash
application
 ┣ port
   ┣ in
   ┃ ┗ dto # dto xử lý request/response từ web
   ┃   ┣ request
   ┃   ┃ ┗ AuthRequestDto.java
   ┃   ┗ response
   ┃     ┣ AuthResponseDto.java
   ┃     ┗ ErrorResponse.java
   ┃
   ┗ out
     ┣ gateway # OpenFeign client
       ┣ dto # dto xử lý request/response tới service khác
       ┃ ┣ request # request gửi sang service khác
       ┃ ┃ ┗ WelcomeMailRequest.java
       ┃ ┗ response # response nhận về từ service khác
       ┃
       ┗ NotificationGateway.java # Interface khai báo hàm gọi sang client
```

## **`2.` Gateway _Interface_**

Do `/application` không thể phụ thuộc vào `/infrastructure`. Cụ thể, không nên inject `NotificationClient`, vào tầng `@Service` nên ta phải sử dụng: `Gateway Interface` + `Adapter`

- Gateway interface ở [application/port/out/gateway/NotificationGateway.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/application/port/out/gateway/NotificationGateway.java)

  ```java
  // Synchronous communication
  public interface NotificationGateway {

      // hàm gọi client
      // thay void bằng kiểu trả về phù hợp nếu có
      public void sendWelcomeMail(
            // dto để gọi service khác
            // khác với dto của web
            WelcomeMailRequest request
      );

  }
  ```

- Client class ở [infrastructure/client/NotificationClient.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/client/NotificationClient.java)

  ```java
  @FeignClient(
          name = "notification-service",
          url = "${app.client.notification-service.url}", // nếu cấu hình API Gateway / Load Balance thì không cần nữa

          // configuration
          configuration = FeignConfig.class
  )
  public interface NotificationClient {
      // chỉ cần khai báo interface
      // Spring tự sinh Proxy khi cần, tường tự JpaRepository

      @PostMapping(
              value = "/notifications/welcome"

              // upload multipart
              // consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      )
      void sendWelcomeMessage(
              // RequestBody, RequestParam,
              // PathVariable
              @RequestBody WelcomeMailRequest request
      );
  }
  ```

- Adapter: [infrastructure/gateway/NotificationGatewayAdapter.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/gateway/NotificationGatewayAdapter.java)

  ```java
  @RequiredArgsConstructor
  @Component
  public class NotificationGatewayAdapter implements NotificationGateway {

      // adapter giữa client và application/gateway
      private final NotificationClient client;

      @Override
      public void sendWelcomeMail(WelcomeMailRequest request) {

          // processing

          client.sendWelcomeMessage(request);

          // nếu có result
          // => result processing,
          // mapping application/port/out/gateway/dto/response
          // return result
      }
  }
  ```
