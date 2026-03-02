# **Implement** Spring **_security_** using `JWT` authentication

implements:

- `JWT` authentication
- authorization by `User.role`

**dependencies**:

```kotlin
// Spring security
implementation("org.springframework.boot:spring-boot-starter-security")
// JWT -> jjwt
implementation("io.jsonwebtoken:jjwt-api:0.13.0")
implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
implementation("io.jsonwebtoken:jjwt-jackson:0.13.0") // JSON parse
```

details: [build.gradle.kts](/codes/mini-project/build.gradle.kts)

## **step-by-step**:

### **`Step 1`:** Chuẩn hóa Domain Model & UserDetailService

details: [User.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/model/User.kt)

sumary:

- add `role` (for authorization)
- extends `UserDetails`

> _**Spring Security** là một framework được viết sẵn. Nó được thiết kế để phục vụ hàng triệu dự án trên thế giới._

Nó không quan tâm User hay Account, id là UUID, hay String, Long, ... nó cần:

- `password` -> authentication
- `role` -> authorization

Vì vậy, **Spring** sinh ra `UserDetails` làm **tiêu chuẩn** và yêu cầu User, ... **extends** class này và **override các methods** của nó.

---

### **`Step 2`:** tạo `service` giúp `UserDetails` lấy data

details: [core/config/ApplicationConfig.kd](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/ApplicationConfig.kt)

- **step 3:** `JWT`
  - **3.1. generate secret key**
    ```cmd
    openssl rand -base64 32
    ```
    run on `cmd`
  - **3.2. key configuration** :

    > _copy result of above command and past to: [application.properties](/codes/mini-project/src/main/resources/application.properties)_

  - **3.3. create [core.security.JwtService.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/core/security/JwtService.kt)**

- **step 4:** add **JwtAuthenticationFilter**

  details: [core.security.JwtAuthentiactionFilter.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/core/security/JwtAuthenticationFilter.kt)

- **step 5: Configuration**
  - **5.1: PasswordEncoder, authProvider, authManager config**:

    details [core/config/ApplicationConfig.kd](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/ApplicationConfig.kt)

  - **5.2: security config**

    details: [config/SecurityConfig.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/SecurityConfig.kt)

- **step 6: controller**
  - dto:
    - [dto.request.LoginRequest.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/request/LoginRequest.kt)
    - [dto.request.AuthResponse.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/response/AuthResponse.kt)
  - [AuthService.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/AuthService.kt)
