# Pagination & Filtering

## **1. Pagination - `Phân trang` dữ liệu**

> _`Pagination` - phân trang : chia `data` thành từng trang (**page**) thay vì trả toàn bộ_

**Vấn đề**: giả sử:

- **users** có `1.000.000 rows` trong **DB**
- **Frontend** gửi request tới API `GET /users`
- **Controller** gọi: `userRepository.findAll()`

Khi này:

- `1M records` được lấy từ DB -> **memory**:
  - **query** chậm
  - tải **database** lớn
  - tốn **memory**
    > có thể gây **crash** / **giảm performance** của server
- response:
  - **big data** -> tốn **network** / **response-time** lớn
  - Frontend render **lag** do **request phản hồi chậm**

=> **Solutions: `pagination`**

## **2. Filtering - Tìm kiếm `có mục tiêu`**

> _Cho phép **Client** yêu cầu kèm điều kiện của data phản hồi_

## **3. Implementation**

#### **Step 1**: Custom `Page Response DTO`

> Thay vì trả trực tiếp `Page<Entity>` mặc đinh (bao gồm cả `pageable`, `sort`, `empty`, ... mà **client không cần**), cần bọc vào trong **contaier: `PageResponseDto`**

details: [PageResponseDto.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/response/PageResponseDto.kt)

#### **Step 2**: enable **Pageable** in `repository`

details: [UserRepository.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/repository/UserRepository.kt)

#### **Step 3**: sử dụng trong `service`

details: [UserServiceImpl.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/impl/v1/UserServiceImpl.kt)

#### **Step 4**: thiết kế `controller`

details: [UserController.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/controller/UserController.kt)

## **4. Dynamic `Filtering` & `Sorting`**

> _Có thể hứng cả cục `Pageable` ở controller :))))))))))))))))_

### **4.1. Dynamic Filtering**

- **Step 1:** [dto.request.UserFilter.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/dto/request/UserFilter.kt)
  > _nên định nghĩa `filter -> command` vì đều là `dto`_
- **Step 2:** gắn `JpaSpecificationExecutor` vào [UserRepository.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/repository/UserRepository.kt) -> cho phép repository có thể **Dynamic Filtering**
- **Step 3:** [NullSpecification.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/repository/specification/NullSpecification.kt)

  ```kotlin
  // normal specification
  val spec = Specification.where(spec1).and(spec2).and(spec3)....
  // nhưng,
  // nói chung là do Kotlin không cho truyền null vào non-nullable
  // mà .where(), .and() nhận non-null
  // nên cần làm gì đó để thêm null vào :)

  // ex: xem
  // NullSpecification.kt
  ```

- **Step 4:** hứng `Filter` và `Pageable` tại `controller`: [UserController.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/controller/UserController.kt)
- **Step 5:** xử lý tại `service`: [UserServiceImpl.kt](/codes/mini-project/src/main/kotlin/com/vduczz/mini_project/service/impl/v1/UserServiceImpl.kt)
