# Project structure

### **Overview**

```markdown
demo-backend/
├── build.gradle.kts        <-- khai báo thư viện, tương tự package.json
├── settings.gradle.kts     <-- cấu hình tên project cho Gradle
├── src/
│ ├── main/                 <-- app
│ │ ├── kotlin/com/.../     <-- codes...
│ │ └── resources/          <-- static file / config
│ │ ├── application.yml     <-- configuration file, ~ .env (có thể là application.properties)
│ │ └── static/             <-- resources: media, html, css
│ └── test/                 <-- Unit Test
```

### **`src/main/kotlin/com/your_name/project_name`: application** => `3-Tire` Architecture

> _Các package ngang hàng với ProjectNameApplication.kt_

- `entity` / `model`-> chứa các class thực thể ứng với thực thể trong DB / business
- `repository` -> interface interact with database, chỉ chịu trách nhiệm CRUD
- `service` -> Business logic
- `controller` -> tiếp nhận Request từ client và gọi Service xử lý, sau đó trả Response
- `dto` -> Data Transfer Object
  - Chứa các class đóng vai trò là container cho dữ liệu của Request và Response
  - **Rule**: không bao giờ trả thẳng Entity từ DB ra API nhằm tránh rò rỉ dữ liệu nhạy cảm
- `exception` -> chứa các class gom và bắt lỗi cho toàn hệ thống => Global Exception Handling
- `config` -> configuration files (Spring Security, CORS, ... )

> _Data Flow_:

1. Client send Request (+ JSON)
2. Controller nhận Request, map JSON -> DTO object
3. Controller gọi Service và gửi DTO cho service xử lý
4. Service thực thi: logic tính toán, validation, ... -> chuyển DTO thành Entity
5. Service gọi Repository -> DB
6. Return flow: DB -> Repository -> Service (Entity -> DTO) -> Controller -> Client
