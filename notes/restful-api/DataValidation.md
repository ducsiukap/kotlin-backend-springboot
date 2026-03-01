# Data `validation`

> **Nguyên tắc**: _KHÔNG BAO GIỜ tin tưởng **Frontend** => **`data` trước khi vào hệ thống cần được `validate`**_.  
> Ngược lại, ở chiều ra, data khi đi từ hệ thống ra không cần phải validate

### **1. Dependencies**: [build.gradle.kts](/codes/mini-project/build.gradle.kts)

> _`org.springframework.boot:spring-boot-starter-validation`_

### **2. Implementation**

Thông thường, `validations` được gắn vào **DTO Classes**

> **DTO validation**: [CreateUserRequest.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/request/UserRequestDto.kt)  
> **sau đó, nó được `@Valid` ở `controller`**: [UserController.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/controller/UserController.kt)

Tuy nhiên, nó cũng có thể `@Valid` ở **function's parameter** của `service`, **function's params** (**không phải `dto`**) của `controller`, hoặc **field** của `entity`

```kotlin
override fun createUser(
    // gắn validation ở params của service
    @Valid command: CreateUserCommand
) {}
```

**2.1. Annotations**

- **String** validation
  - **null/empty**

    | **Annotaion** | `null` | **empty** | **blank str** (ex: " ") |
    | :-----------: | :----: | :-------: | :---------------------: |
    |  `@NotNull`   |   ❌   |    ✅     |           ✅            |
    |  `@NotEmpty`  |   ❌   |    ❌     |           ✅            |
    |  `@NotBlank`  |   ❌   |    ❌     |           ❌            |

  - `@Size`: requires size-limits
  - `@Email`: requires email-format
  - `@Pattern`: requires regex-matching

- **Number** validation
  - `@Positive`: require positive number
  - `@PositiveOrZero`, `@Negative`, `@NegativeOrZero`
  - `@Min` / `@Max`: requires Min/Max value
- **Date-time** validation
  - `@Past` / `@PastOrPresent`: thời gian bắt buộc ở quá khứ / ở quá khứ hoặc hiện tại
  - `@Future` / `@FutureOrPresent`: thời gian bắt buộc ở tương lai / ở tương lai or hiện tại
- **Boolean** validation
  - `@AssertTrue`: buộc phải `true`
  - `@AssertFlase`: buộc phải `false`

**2.2. Nested validation**

> _Khi `dto` **chứa một dto khác có validation**_

```kotlin
data class AddressDto(
    @field:NotBlank(message="String is required")
    val street: String
)

//
data class UserRequest() (
    // ...

    // AddressDto có validation
    // -> phải dùng @Valid để yêu cầu check
    @field:Valid
    val address: AddressDto
    // có thể dùng cho cả List<> ...
    @field:Valid
    @field:NotEmpty(message="addressList must have least one address")
    val addressList: List<AddressDto>
)
```
