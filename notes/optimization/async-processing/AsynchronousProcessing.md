# `@Async` processing

## **_Asynchronous_** processing

- `synchronous` processing: là cơ chế hệ thống xử lý tuần tự, lần lượt.

  > _**`synchronous`** là **luồng chạy mặc định** của **mọi API**, nơi thread của request phải bị **block** và chờ controller xử lý xong mới được quyền trả về cho client và thực hiện task khác._

- `asynchronous` processing: là **cơ chế** cho phép hệ thống **_làm nhiều task cùng lúc_** mà không phải chờ task khác.

  > _**`asynchronous`** cho phép server chỉ cần thực hiện **core-business-logic** (các task **bắt buộc phải hoàn thành** theo luồng nghiệp vụ), sau đó đưa các task không bắt buộc phải thành công (như gửi notification, ...) vào **`Background Thread`** và phản hồi cho client ngay._

  Nhờ vậy, server có thể:
  - **Low Latency**: giảm thời gian phản hồi mà client phải chờ.
  - **Tiết kiệm tài nguyên**: giải phóng thread và phục vụ được nhiều client hơn.

  **Nhược điểm**:
  - **Khó debug**: do logic chạy trên 2 hoặc nhiều thread => khó quản lý việc thành công 100%.
  - **Không kiểm soát được exception**: main thread không thể dùng `try-catch` để quản lý exception được ném ra từ luồng phụ.

## **Implementation**

### **1. `Thread Pool` configuration**

Mặc đinh, **Spring Boot** sử dụng `SimpleAsyncTaskExecutor` -> sinh ra 1 thread mới cho mỗi request.

> _Nếu lượng request quá lớn => hết RAM => crash server_

Vì vậy, cần giới hạn số lượng luồng: [config/AsyncConfig.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/config/AsyncConfig.kt)

### **2. `@Async` task**

Details: [MailNotificationServiceImpl.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/impl/v1/MailNotificationServiceImpl.kt)

**Rules**:

- Không được gọi `@Async` từ **_hàm khác nằm trong cùng class_** (vì trong cùng class thì nó chạy code thật của class, không đi qua **Proxy** -> vẫn chạy đồng bộ)
- Hàm `@Async` bắt buộc là `public`

### **3. invoke `@Async` method in `other service`**: [/service/impl/v1/AuthService.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/impl/v1/AuthServiceImpl.kt)
