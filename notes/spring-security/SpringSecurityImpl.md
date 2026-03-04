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
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0") // JSON parse
```

details: [build.gradle.kts](/codes/mini-project/build.gradle.kts)

## **step-by-step**:

### **`Step 1`:** Chuẩn hóa Domain Model

details: [User.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/model/User.kt)

sumary:

- add `role` (for authorization)
- extends `UserDetails`

> _**Spring Security** là một framework được viết sẵn. Nó được thiết kế để phục vụ hàng triệu dự án trên thế giới._

Nó không quan tâm User hay Account, id là UUID, hay String, Long, ... nó cần:

- `password` -> authentication
- `role` -> authorization

Vì vậy, **Spring** sinh ra `UserDetails` làm **tiêu chuẩn** và yêu cầu User, ... **extends** class này và **override các methods** của nó.

#### **Notes**: giải quyết vấn đề **có nhiều loại User**:

- **Cách 1**: gộp tất cả các loại `users` vào `User`
  - Ưu: Nhanh, gọn
  - Nhược: Thiếu linh hoạt nếu mỗi loại user có **thuộc tính riêng** -> bảng `users` phình to
- **Cách 2**: Phân tách **Tài khoản đăng nhập (`Account`)** ra khỏi **Hồ sơ người dùng (`Profile`)**
  - Bước 1: tạo `Account` chứa `username`, `password`, `role`
    > _**DUY NHẤT** `Account` phải implements `UserDetails`_
  - Bước 2: tạo class riêng cho ngiệp vụ -> `UserProfile`, `AdminProfile`, ...
    > _**Mỗi class** có `@OneToOne` tới `Account`_

  > _**Quy trình**: Security tiếp nhận request => lấy `username` => gọi **Account** để lấy account => check `password` => truyền vào `controller` để lấy profile tương ứng_

  **Cách 2 là cách XỊN NHẤT**

  Khi này:
  - khi **login**, client gửi chung lên `/auth` -> nhận về `accountId`, `token`, `role`, ..
  - sau khi có thông tin xác thực, bao gồm `token`, và `role` phản hồi từ **server**, **mọi request từ client liên quan tới profile, ... sẽ đi tới url tương ứng với `role`**:
    - client dựa vào `role` (giả sử `role=CUSTOMER`) để phân trang cho người dùng, sau đó gửi `/customers/me` (**The `Me` Endpoint**) kèm `token` để lấy thông tin cá nhân.
    - mọi request sau sẽ đi tới `/customers/...` nếu request chỉ dành cho customer

- **Cách 3**: tận dụng `Inheritance` của `JPA` theo chuẩn **OOP**

  ```kotlin
  // Step 1: create BaseUser class
  // sử dụng Annotation:
  //    @Inheritance(strategy = InheritanceType.JOINED)
  // ý nghĩa: khi gọi baseUserRepo.findByUsername(username)
  //    => Hibernate tự động JOIN các bảng lại để lấy đúng data
  @Inheritance(strategy = InheritanceType.JOINED)
  class BaseUser(
    var username: String,
    var password: String,
    val role: UserRole
  )

  // Step 2: create sub-classes extends BaseUser
  class Admin(...): BaseUser(...)
  ```

---

### **`Step 2`:** **UserDetailService** -> tạo service cho phép SpringSecurity lấy data từ `DB`

> _Spring Security không biết cách xuống db lấy data -> **yêu cầu cung cấp `@Bean` `UserDetailsService`** hỗ trợ lấy entity từ DB_

details: [config/ApplicationConfig.kd](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/ApplicationConfig.kt)

---

### **`Step 3`:** `JWT` service -> **JWT Utility Component**

**Utility Class** có vai trò:

- `Serialization` - **Generate**: lấy `UserDetail` object (và các thông tin khác nếu có - `claims`) và ký thành chuỗi `JWT-token` (**Header.Payload.Signature**)
- `Deserialization` - **Extract** & `Validation` - **Verify** :
  - nhận `jwt-token`
  - verify thuật toán kí bằng `SecretKey`
  - parse ra `Claims` (payload)
  - validate thời gian (expiration)
  - ...

**Implementation**:

- **3.1. generate secret key**
  ```cmd
  openssl rand -base64 32
  ```
  run on `cmd`
- **3.2. key configuration** :

  > _copy result of above command and past to: [application.properties](/codes/mini-project/src/main/resources/application.properties)_

- **3.3. create JwtService**: generate, extract & verify `jwt-token`

  details: [core.security.JwtService.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/core/security/JwtService.kt)

- **step 4: create `JWT authentication Filter`**

  details: [core.security.JwtAuthentiactionFilter.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/core/security/JwtAuthenticationFilter.kt)

  **Scope of work**: Thêm `jwt-filter-chain` vào FilterChain của quy trình Authentication
  - Bắt **MỌI** `HTTP Request` đi qua tầng **Servlet**
  - Trích xuất Header `Authorization`
  - Dùng `JwtService` để verify token & expiration. Nếu hợp lệ, load `UserDetails` object và set `UsernameAndPasswordAuthenticationToken` vào `SecurityContextHolder`.

- **step 5: Configuration**: cấu hình `SecurityFilterChain` (**SecurityConfig**)
  - **5.1: PasswordEncoder, authProvider, authManager config**:

    details [core/config/ApplicationConfig.kd](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/ApplicationConfig.kt)

  - **5.2: security config**

    details: [config/SecurityConfig.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/SecurityConfig.kt)

- **step 6: implementation Auth endpoint**
  - dto:
    - [dto.request.LoginRequest.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/request/LoginRequest.kt)
    - [dto.request.AuthResponse.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/response/AuthResponse.kt)
  - service: [AuthServiceImpl.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/impl/v1/AuthServiceImpl.kt)
  - controller: [AuthController.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/controller/AuthController.kt)

  about **`AUTHORIZATION`**:
  - **Simple Authorize** - RBAC:
    - **Cách 1**: config ở [config/SecurityConfig.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/SecurityConfig.kt)

      ```kotlin
      // use:
      http.authorizeHttpRequests { auth ->
        auth
          .requestMatchers(
            // urls
          )
          .hasRole("ROLE") // for specific role, such as ROLE_USER
          // or
          .hasAnyRole("ROLE1", "ROLE2") // for mutliple roles, such as: ROLE_USER, ROLE_ADMIN
      }
      // tuy nhiên, không quá khuyến khích
      ```

    - **Cách 2**: authorize at `controller` -> `@EnableMethodSecurity` + `@PreAuthorize`:

      ```kotlin
      // first
      // at config/SecurityConfig.kt
      @EnableMethodSecurity // annotation to enable method security
      SecurityConfig(...) {}

      // second
      // authorize at controller class
      // use: @PreAuthorize at top of class/method
      @PreAuthorize("hasRole(ADMIN)") // require ADMIN-role
      @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // accessible for multiple roles
      // or SpEL - Spring Expression Language
      @PreAuthorize("""
        hasRole('ADMIN')
        or
        authentication.principle.id.toString() == #userId
      """)
      @PutMapping("/{userId}")
      // -> role=ADMIN
      //    hoặc authenticated user có id = chính userId đang request (đúng profile của mình)
      ```

  - **Complex / Data-driven authorization**
    - **Cách 1**: `@EnableMethodSecurity` + `CustomChecker` + `@PreAuthorize`
      - enable: `@EnableMethodSecurity` at [/config/SecurityConfig.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/SecurityConfig.kt)
      - custom checker: [/core/security/checker/UserSecurityChecker.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/core/security/checker/UserSecurityChecker.kt)
      - use `@PreAuthorize` at `controller`: [UserController.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/controller/UserController.kt)

    - **Cách 2**: use `@AuthenticationPrinciple`
      - service: [UserServiceImple.deleteUser](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/impl/v1/UserServiceImpl.kt)
      - controller: [UserController.deleteUser](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/controller/UserController.kt)
