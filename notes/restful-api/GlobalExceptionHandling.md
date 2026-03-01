# Global Exception Handling - Xử lý ngoại lệ tập trung

## **1. Default JSON Response**

> _**Spring Boot** cho phép `throw Exception` ở `controller` để **dừng luồng chạy (thread)** (không crash server)_

**Bản chất**: khi exception xảy ra, **Spring Boot** (`Tomcat` server) mặc định sẽ trả về cho **Client** **json-response** dạng:

```json
// example Response
{
  "timestamp": "2026-03-01T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "trace": "org.springframework.web.bind.MethodArgumentNotValidException: Validation failed...",
  "message": "Validation failed for object='createUserRequest'. Error count: 1",
  "path": "/api/v1/users"
}
```

> _**Frontend** không thể dùng `message` để hiển thị cho user (vì **too technically**, vốn dĩ chỉ dùng để dev debug, ...) -> không biết `field` nào sai?_

## **2. Global Exception Handling**

> _**Global Exception Handling** là **mechanism** dư trên tư duy `AOP - Aspect-Oriented-Programming` (**Lập trình hướng khía cạnh**)_

Thay vì phải viết `try-catch` ở controller để handle các exceptions khác nhau và trả về lỗi tương ứng cho client, ta chỉ cần tạo **1 FILE DUY NHẤT** có nhiệm vụ _**bắt và xử lý toàn bộ exception ở tầng controller**_

**Annotations**

- `@RestControllerAdvice`: sử dụng ở đầu **Exception Handler class** để cho phép nó tự động bắt các exception
- `@ExceptionHandler`: gắn ở đầu hàm -> chỉ định loại exception mà hàm có thể bắt

## **3. Implementation**

- defines **Error Response DTO**: [dto.response.ErrorResponse.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/response/ErrorResponse.kt)
- defines **exception handler**: [core.exception.GlobalExceptionHandler.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/core/exception/GlobalExceptionHandler.kt)

**Custom Exception** (optional but strongly recommended)

- defines response dto
- defines custom exceptions: [core.exception.BaseAppException.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/core/exception/BaseAppException.kt)
- defines global exception handler
- use custom exception in `service`: [UserService.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/impl/v1/UserServiceImpl.kt)
