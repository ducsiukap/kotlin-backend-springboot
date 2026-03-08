# **_Layered_** (phân tầng) Architecture

## **1. Bản chất**

### **1.1. App Components**

**Layered Architecture** là kiến trúc chia ứng dụng thành **các tầng** - `layers`, mỗi tầng có **trách nhiệm riêng**. Bao gồm:

- **Presentation layer** - Tầng giao tiếp / `controller`:
  - **Nhiệm vụ**: **giao tiếp với client**.
- **Business Logic layer** - Tầng nghiệp vụ / `service`:
  - **Nhiệm vụ**: là **"trái tim"** của ứng dụng, xử lý mọi quy tắc nghiệp vụ.
  - **Đặc điểm**: Nhận lệnh từ `controller`, xử lý và gọi `repository` để thao tác dữ liệu.
- **Data Access layer** - Tầng truy xuất dữ liệu - `repository` / `dao`
  - **Nhiệm vụ**: tương tác trực tiếp với Database, chứa **SQL Commands** hoặc **ORM Methods** để truy vấn, thêm, sửa, xóa, ... dữ liệu.
- **Domain/Model layer** - Tầng lưu trữ dữ liệu -> **Database**

### **1.2. Principle**

**Principle**: _**Layer** chỉ được gọi (**phụ thuộc**) vào **Layer phía dưới**_

> _**Phụ thuộc hướng vào trong**_

```plaintext
Client
  ↓
Controller Layer
  ↓
Service Layer
  ↓
Repository Layer
  ↓
Database
```

**Data flow**:

1. Client send request to API `GET /resources/1`
2. Resource's Controller (`controller`) receives request, extract data and calls Resource's Service to process.
3. Resource's Service (`service`) receives request from `controller`, processes and calls Resouce's Repository to take Resource's Entity.
4. Resource's Repository (`repository`) queries Database, packs it into an entity and return to `service`
5. `service` takes entity from `repository`, maps to response object and return to `controller`
6. `controller` return resource to Client.

---

## **2. Project structure**

```plaintext
src/main/java/com/example/backend/
├── BackendApplication.java       <-- App's entry point
│
├── controller/                   <-- Presentation layer
│   ├── DeviceController.java
│   └── UserController.java
│
├── service/                      <-- Business logic layer
│   ├── DeviceService.java        <-- Interfaces
│   ├── UserService.java
│   └── impl/                     <-- Implementation classes
│       ├── DeviceServiceImpl.java
│       └── UserServiceImpl.java
│
├── repository/                   <-- Data Access layer
│   ├── DeviceRepository.java
│   └── UserRepository.java
│
├── entity/                       <-- Entity/Model (Database)
│   ├── Device.java
│   └── User.java
│
├── dto/                          <-- Data Transfer Obj(interact w Client)
│   ├── request/                  <-- // Client request
│   │   └── DeviceRequestDTO.java
│   └── response/                 <-- // Controller response
│       └── DeviceResponseDTO.java
│
├── config/                       <-- configuration: security, swagger, db, ...
│   └── SecurityConfig.java
│
└── exception/                    <-- global exception
    ├── GlobalExceptionHandler.java
    └── DeviceNotFoundException.java
```

## **3. _Layered_ architecture + _DDD_ Design Approach**

Khác với **Data-Driven Design** áp dụng `3-Tier` truyền thống, bao gồm **Controller**, **Service** và **Repository**  
khi áp dụng **Domain-Driven Design**, application được chia `4-Tier`:

- **User Interface / Presentation Layer**
  ```plaintext
  │
  ├── interface/
  │   ├── controller/
  │   │   └── LockController.java
  │   └── dto/
  │       ├── request/
  │       │   └── UnlockRequest.java
  │       └── response/
  │           └── LockResponse.java
  ```
- **Application Layer**: điểm khác biệt lớn nhất là **Service** lúc này chỉ đóng vai trò điều phối, không thực thi các **business-logic**
  ```plaintext
  ├── application/
  │   ├── command/
  │   │   └── UnlockCommand.java          // dto.request -> command
  │   └── service/
  │       └── LockApplicationService.java // điều phối
  ```
- **Domain Layer**: thay thế cho `service` để trở thành **"trái tim"** của hệ thống, chứa `Rich Domain Model` (mô hình miền chứa cả dữ liệu lẫn hành vi), Value Object, Domain Event, ...

  Mọi logic phức tạp liên quan tới model đều nằm ở Domain

  ```plaintext
  ├── domain/
  │   ├── model/
  │   │   ├── SmartLock.java              // chứa cả data & logic (ex: unlock())
  │   │   └── PinCode.java                // Value Object (optional)
  │   ├── repository/
  │   │   └── LockRepository.java         // interface, cung cấp hàm lưu/lấy data, không cần biết SQLX
  │   └── exception/
  │       └── LowBatteryException.java    // business exceptions
  ```

- **Infrastructure Layer** - hạ tầng: mọi thứ liên quan tới kỹ thuật ngoại vi như lưu trữ db (Spring Data JPA), gửi thông báo qua Kafka, Api calls, ...

  ```plaintext
  ├── infrastructure/                     // Công nghệ, Framework, DB
  │  ├── persistence/
  │  │   ├── entity/
  │  │   │   └── SmartLockEntity.java    // mapping db table
  │  │   ├── mapper/
  │  │   │   └── LockDataMapper.java     // mapper entity - model
  │  │   └── repository/
  │  │       ├── JpaSpringLockRepo.java  // spring data jpa
  │  │       └── LockRepositoryImpl.java // Implements domain repository
  │  ├── config/
  │  │   └── SecurityConfig.java         // App's configurations
  │  └── gateway/
  │  │   └── MqttIoTClient.java          //
  │
  │
  └── config/                           // (optional) project config, ...
  ```

> _**Tư duy** của `DDD` là **bảo vệ `Domain Model`**, vì vậy có thể linh hoạt bỏ `command` (write-request) /`query` (read-request) ở `/application` và đưa `dto` vào `/application` thay con **command**_

**Sample Project**: [https://github.com/citerus/dddsample-core](https://github.com/citerus/dddsample-core)
