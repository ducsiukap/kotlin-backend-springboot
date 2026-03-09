# System **_Architectures_**

**`System Architecture`** - **Kiến trúc hệ thống**: là _sự quyết định_ và _định nghĩa_ **các thành phần cốt lõi của hệ thống** và **cách chúng giao tiếp với nhau** để đáp ứng được yêu cầu của dự án. Cụ thể:

- **`Components` - Thành phần**: là các `block` cấu tạo nên hệ thống, có thể là: `Server`, `Database`, `Frontend`, `Cache`, `Message Queue`, `Search Engine`, `Cloud Storage`,....
- **`Interactions` / `Relationships` - Sự tương tác**: là cách dự liệu chảy từ:
  - người dùng xuống server
  - server xuống db và ngược lại

  hoặc cách các module giao tiếp với nhau qua `REST API`, `gRPC`, `Message Broker`, ...

**Sự quan trọng**: _thiết kế hệ thống_ quan trọng bởi vì:

- **Quyết định khả năng của hệ thống**: `scalability`, `performance`, `security`, `reliability`, ... -> **_Các yêu cầu phi chức năng - `Non-Functional Requirements`_**
- **Định hướng cho team**
- **Kiểm soát chi phí**

# 1. Kiến trúc **_tổng thể_** hệ thống - `Macro level / System architecture`

- `Monolithic Architecture` / `Modular Monolithic Architecture` - **kiến trúc nguyên khối**: tất cả mọi thứ, bao gồm:
  - Xử lý API
  - Business logic
  - Database query
  - Frontent
  - ...

  `monolithic`:

  ```plaintext
  Web Application
    ├─ UI
    ├─ Business Logic
    ├─ Data Access
    └─ Database
  ```

  `modular-monolithic`:

  ```plaintext
  Application

    ├─ user-module
    ├─ order-module
    └─ payment-module
  ```

  hay nói các khác, **toàn bộ các thành phần của 1 ứng dụng** nằm trong **_chung 1 bộ source code duy nhất_** - viết trong **1 application**.

  **Ưu điểm**:
  - Dễ setup ban đầu
  - Dễ deloy

  **Nhược điểm**:
  - Phù hợp với dự án **vừa** và **nhỏ**

- `Microservices Architecture` - **Kiến trúc hướng dịch vụ** : là **tiêu chuẩn** của các hệ thống lớn hiện nay.

  Cụ thể, thay vì gom toàn bộ application vào chung 1 bộ source code, hệ thống được tách thành các **`micro-service` - dịch vụ nhỏ**, hoạt động hoàn toàn độc lập. Mỗi service có thể:
  - tự quản lý db
  - dùng ngôn ngữ lập trình riêng
  - deloy tách biệt

  ```plaintext
  Frontend
    ↓
  API Gateway
    ↓
  Services
    ├─ User Service
    ├─ Order Service
    ├─ Payment Service
    └─ Product Service
  ```

  Details: [Microservice Architecture](./microservices/README.md)

- `Event-Driven Architecture` - **Kiến trúc hướng sự kiện**: các thành phần trong hệ thống không gọi nhau trực tiếp, ví dụ qua **API calls**.

  Thay vào đó, các block giao tiếp với nhau qua cơ chế:
  - `publish` - phát sự kiện
  - `subcribe` - lắng nghe sự kiện

  qua các `message broke` như **Kafka**, **RabbitMQ**

  **Event-Driven** cực kì mạnh mẽ cho các **_hệ thống phân tán_** hoặc cần **_xử lý real-time_**.

- `Serverless Architecture`

# 2. Kiến trúc bên trong **`mã nguồn`** - `Micro level / Application Architecture`

### **2.1. _Tightly Coupled_ Architecture**

- **Model - View - Controller** - `MVC` - **Server-side rendering**: gom chung tất cả FE và BE vào 1 application, cụ thể:
  - **Model**: chứa data và logic tương tác với DB
  - **Controller**: nhận request từ trình duyệt web của người dùng và trả về **View**
  - **View**: giao diện

  > _**MVC cổ điển** là kiến trúc cũ, nơi server phải làm toàn bộ mọi thứ, từ xử lý logic cho tới tạo giao diện, ..._
  >
  > _**MVC hiện đại** có thể coi phần view là controller response (JSON)_

- **Page Controller**
- **Template-based**
- **Front Controller**

### **2.2. _Decoupled_ Architecture** - API-Driven / Client-Server Architecture

Bản chất của `decoupled` là chia application ra làm 2 thành phần:

- `backend`: xử lý logic, quản lí và nhả ra dữ liệu
- `frontend`: thể hiện dữ liệu và tương tác với **end-user**

#### **2.2.1. _`Frontend`_ Application Architecture**

- **`MVVM` - Model-View-ViewModel`**: dành cho **MobileApp**, trong đó:
  - **Model**: tầng chứa dữ liệu của client, chịu trách nhiệm gọi API hoặc đọc từ local database
  - **View**: giao diện người dùng
  - **ViewModel**: cầu nối giữa Model và View, giữ trạng thái - **UiState**, ...
- **`Flux` / `Redux`** - **`SPA`**: thịnh hành trong **WebApplication** - `Web SPA`

#### **2.2.2. _`Backend`_ Application Architecture**

- **Layered Architecture** - `N-tier`: trực quan và dễ tiếp cận, **phù hợp để bắt đầu**, là nền tảng của backend.

  Details: [Layered Architecture](./n-tier/README.md)

- **Clean Architecture**: được dùng nhiều khi chia hệ thống thành các **micro-services**, không phụ thuộc Framework.

  Details: [Clean Architecture](./clean/README.md)

- **Hexagonal Architecture**
