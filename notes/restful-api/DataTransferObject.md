# `DTO` - Data Transfer Object

#### **Bản chất `DTO`**

> _`DTO` - **Data Transfer Object** - đối tượng chuyển đổi (vân chuyển) dữ liệu._

- DTO chỉ có vai trò duy nhất: chuyển đổi dữ liệu, đóng vai trò là container đóng gói dữ liệu trao đổi (thường là giữa `service` và `controller`)
- **KHÔNG** chứa business logic
- **KHÔNG** gán `@Entity`, `@Table`, .. hay liên kết với **Database**

> _Nguyên tắc: **tuyệt đối *KHÔNG* sử dụng `Entity` làm `Request`/`Response`**_

## Why need `DTO`?

- **`Security`**: Entity chứa những `sensitive-field` - dữ liệu nhạy cảm - như **password** của **User**, ... => _**Nhiệm vụ của `dto` là che dấu các dữ liệu nhạy cảm này**_
- **`Infinite Recursion`**: giả sử, trong `entity`, User và Post **tham chiếu lẫn nhau** -> khi chuyển thành JSON dẫn tới `User -> Post -> User -> Post -> ... (Infinity Recursion)`. _**Các lớp `dto` có vai trò cắt đứt vòng lặp này**_
- **`Decoupling`**: độc lập với DB.
- Tránh việc tự sinh ra `id`, `createdAt` khi tạo mới **Entity** => _**`dto` classes chỉ lấy các trường cần thiết (`projection`) từ Entity**_
- Tận dụng được `data class` của Kotlin

## Implementations

- Define `dto` classes:
  - [UserRequestDto.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/request/UserRequestDto.kt)
  - [UserResponseDto.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/response/UserResponseDto.kt)

  `derived-field` nên đặt ở:
  - **Entity** nếu `@Service` cần nó để tính toán logic

    > _ex: `UserService` cần `age` của **User** để quyết định logic_

  - **DTO** nếu chỉ cần để làm logic hiển thị:
    > _ex: `Client` cần `fullName` chứ không cần `firstName`/`lastName` của **User**_

- `Command` **-** `DTO` mapping:
  - using `extension-function`:
    - [UserRequestDto.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/request/UserRequestDto.kt)
    - [UserResponseDto.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/response/UserResponseDto.kt)
  - or using `MapStruct`

  > **note**: với **nguyên tắc kiến trúc dài hạn** / **Clean Architecuture**, `@Service` là business, nó không nên được biết về DTO, chỉ làm việc với domain -> việc mapping nên diễn ra ở Controller / Mapper

  Cụ thể
  - Controller nhận request: nhận dto -> entity -> service
  - Service trả entity -> controller nhận entity -> dto -> response

  > _Tuy nhiên, với hệ thống đơn giản không cần `multi-protocols` / `reuse` service ở nhiều nơi, việc mapping ở service lại tốt hơn_

  Clean Architecture: sinh ra domain/command để xử lý việc entity-command-dto, service/controller đều sử dụng command

  ```text
  project
    ├── controller/
    ├── dto/
    │   ├── request/
    │   └── response/
    ├── core/
    │   ├── command/
    │   └── exception/
    ├── service/
    ├── model/
    └── repository/


    // request: dto -> command -> entity
    // response: entity -> dto (cho phép truyền trực tiếp, đúng hơn thì dùng entity-model)
  ```
