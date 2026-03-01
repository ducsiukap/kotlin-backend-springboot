# `service` layer

> _`service` là tầng thực thi **business-logic** chính, bao gồm việc tiếp nhận **request** từ `controller`, **xử lý logic**, gọi `repository` để **thao tác với DB** và trả **response** về lại cho `controller`_

### **Naming for `@Service` classes:**

#### **`Entity-based`**:

> _`entity-based` used for **small**/**medium** project where `@Service` class's name is `Entity'sName` + `Service`, such as: **UserService**, **ProductService**, ..._

- **Advantage**: dễ tìm file, quản lý CRUD tiện.
- **Disadvantage**: khi dự án trở nên lớn hơn, `@Service` class phình to -> **God class**

#### **`Use-case-based`** -> class's name based on **business logic**

> _Nguyên tắc `Single Responsibility`, sinh `@Service` class theo nghiệp vụ. ex: `CheckoutService`, `ReportService`, ..._

**Microservices-oriented:** -> `mircroservice architecture`: chia hệ thống ra thành các hệ thống siêu nhỏ (**microservice**).

- Mỗi `microservice` đảm nhiệm nghiệp vụ độc lập có:
  - server riêng
  - DB riêng
- Tên của microservice được đặt theo **hành động**/**nghiệp vụ**

### **implement `service` layer**

- Basic `@Service` implementation:
  - interface: [UserService.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/UserService.kt)
  - class implementations: [UserServiceImpl.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/impl/UserServiceImpl.kt)
- How to use `@Transactional`
  - Với những **method có thao tác với DB** (qua `repository`), nên dùng `@Transaction` (kể cả chỉ `SELECT` - với `readOnly=true` -> performance)
  - Với những **method không thao tác với DB** như:
    - Hàm logic / utils
    - Hàm gọi API third-part

    > _Không nên dùng `@Transactional` cho những method này vì chiếm DB Connection_

  **Triển khai `@Transaction`:**
  - **Cách 1:** Gán lên class: `@Transactional(readOnly=true)`

    ```kotlin
    // example
    @Transactional(readOnly=true)
    class UserService(
        private val UserRepository
    ) {
        // - Mặc định mọi hàm có `@Transactional(readOnly=true)`
        fun getListUsers() : List<User> = userRepository.findAll()

        // hàm ulti/logic cũng chạy trong transaction
        // để khắc phục, có thể sử dụng Propagation.NOT_SUPPORTED
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // không dùng transaction
        fun utilMethod() {
            // ...
        }

        // - Hàm nào cần update db -> ghi đè: `@Transactional(rollbackFor=[Exception::class])`
        @Transactional(rollbackFor=[Exception::class])
        fun createUser(newUser: User) : User {
            // ...
        }
    }
    ```

    > _nhanh nhưng lãng phí **DB connection** nếu không dùng `Propagation.NOT_SUPPORTED`_

  - **Cách 2:** Gán `@Transactional` lên từng method theo logic -> **Tối ưu**, **chính xác** nhưng **dễ quên**, **code dài**
  - **Cách 3:** `SOLID` principle -> tách `@Service` theo **Responsibility** + đặt `@Transactional` lên class => **`có vẻ tối ưu nhất`**

- **Notes**:
  - Thực tế, người ta thường xây dựng `interface UserService` và các class implements như `UserServiceImpl` hay `UserServiceV2Impl`, ...
  - Thường ném `custom exceptions` thay vì `RuntimeException`, ...
