# **_Logging_**

**Vấn đề**: khi **deloy** lên **Docker** / **cloud**, không có terminal để xem log. Vì vậy, `println()`, `exception.printStackTrace()`, .. trở nên **vô hiệu hóa**.

> _**Giải pháp**: mọi thứ phải được **ghi ra `file`**, hoặc tốt hơn là đẩy lên Server quản lý Log tập trung_

**Tools**

- **`SLF4J`** & **Logback**: được sử dụng để:
  - ghi log ra file
  - cắt file theo ngày, theo dung lượng để không làm đầy ổ cứng Server
- **Spring Boot Actuator**: secret-API cho phép theo dõi trạng thái server, bao gồm các thông số về:
  - lượng RAM tiêu thụ
  - lượng Thread đang chạy
  - trạng thái DB
  - ...

### **_Implementation_**

- **Step 1**: dependencies -> [build.gradle.kts](/codes/mini-project/build.gradle.kts)

  ```kotlin
  // Actuator - thư viện theo dõi sức khỏe hệ thống
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  ```

- **Step 2**: **_Logging_** configuration

  details: [application.properties](/codes/mini-project/src/main/resources/application.properties)

- **Step 3**: log in code

  details: [/config/RedisConfig.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/RedisConfig.kt) :: errorHandler()

  **Note**: mỗi class có **_Logger_** riêng, example:

  ```kotlin
  // Logger for RedisConfig class
  private val log = LoggerFactory.getLogger(RedisConfig::class.java)
  ```

- **Step 4**: bỏ authentication require cho API `/actuator/health`
  details:
  - [/config/SecurityConfig.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/SecurityConfig.kt)
  - [/core/security/JwtAuthenticationFilter.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/core/security/JwtAuthenticationFilter.kt)

### **Log _Level_**

- `TRACE`: -> chi tiết, soi tới tận gốc của code // `log.trace()`
  > _gần như **KHÔNG BAO GIỜ DÙNG**_
- `DEBUG`: -> gỡ lỗi -> in thông tin giúp gỡ lỗi lúc code // `log.debug()`
- `INFO`: -> thông tin (**tiêu chuẩn**) : cột mốc quan trọng, chứng tỏ hệ thống vẫn chạy đúng business logic flow // `log.info()`
- `WARN`: -> cảnh báo : luồng code vẫn chạy nhưng có điểm bất thường // `log.warn()`
- `ERROR`: -> lỗi : hệ thống lỗi, văng exception liên tục, luồng nghiệp vụ đứt gãy. // `log.error()`

### Server **_`Healthcheck`_** URL: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
