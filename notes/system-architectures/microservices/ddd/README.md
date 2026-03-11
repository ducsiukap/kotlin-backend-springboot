# _**Domain-Driven**_ Design - `DDD`

## **1. _DDD_**

**DDD** là **triết lí thiết kế** xoay quanh **nghiệp vụ cốt lõi** - `core business logic` / `domain` - được chia thành 2 phần chính **Strategic** (_Chiến lược_) và **Tactical Design** (_Chiến thuật_)

### 1. **_Strategic_ Design** - Thiết kế chiến lược.

Định hình ranh giới giữa các service:

- **Ubiquitous Language** - _Ngôn ngữ chung_: Developers & Business Team bắt buộc phải dùng chung **bộ từ vựng**.
  > Cụ thể, nếu Business gọi là "Khách hàng", trong code **KHÔNG ĐƯỢC** tự ý đổi thành **_Account_** hay **_User_**
- **`Bounded Context` - Ngữ cảnh giới hạn**: là **cách tốt nhất** / **quan trọng nhất** để chia nhỏ hệ thống thành các `microservices`.

  > Cụ thể, với **cùng một thực thể**, đặt ở **ngữ cảnh khác nhau** thì có **_ý nghĩa và thuộc tính hoàn toàn khác nhau_**

  Ex: khái niệm **Product**:
  - **Inventory Context** - ngữ cảnh kho hàng: quan tâm tới trọng lượng, vị trí, số lượng, ...
  - **Sales Context** - ngữ cảnh kinh doanh: quan tâm giá bán, hình ảnh, mô tả

  Vì vậy, nên tách chúng thành 2 service độc lập, thay vì tạo 1 bảng `Product` khổng lồ với quá nhiều thuộc tính.

### 2. **_Tactical_ Design** - Thiết kế chiến thuật.

Dùng các `pattern` để tổ chức code. Trong **domains**, cần quản lý:

- #### **`Entity` - Thực thể**: đối tượng có **định danh duy nhất - `ID`**, vòng đời kéo dài, thuộc tính có thể thay đổi nhưng **định danh không đổi**.

  _Ex_: `Order` có thể có status thay đổi từ **PENDING** thành **SHIPPING** nhưng **luôn** có `ID=123`

- #### **`Value Object` - Đối tượng giá trị**: là object **không có ID**, được định nghĩa hoàn toàn bằng các giá trị bên trong nhó.

  Thường là **immutable**, dùng để mô tả đặc tính.

  Ví dụ trong `UserEntity` có `ShippingAddress` bao gồm _Số nhà_, _Phường_, _Thành phố_, ... Khi này ShippingAddress là thuộc tính của UserEntity, quan tâm tới giá trị của nó chứ không cấp ID độc lập.

  > _**Lợi ích LỚN NHẤT của `Value Object`** là khả năng **Tự xác thực - `Self-validation`** khi mà ta có thể áp dụng các business rule vào quá trình khởi tạo object thay vì phải check ở nhiều nơi._
  >
  > _Bên cạnh đó, khả năng **tái sử dụng** (dùng chung class Value Object làm field của entity chứ không dùng chung ở mức độ instance) cũng là lợi ích nổi bật của Value Object_

- #### **`Aggregate & Aggregate Root` - Cụm / Gốc cụm**: Aggregate là cụm các Entity và Value Object đi liền với nhau để đảm bảo tính toàn vẹn dữ liệu. **Aggregate Root** là `Entity` đại diện, đóng vai trò là cổng giao tiếp duy nhất của cả cụm.

  _Ex_: Có `Order` là Aggregate Root, trong nó chứa danh sách `OrderItem` và `ShippingAddress`

  **Quy tắc**: vì `OrderItem` là thành phần (chặt) của `Order`, mọi **thao tác với OrderItem** phải được **diễn ra ở Root's Repository**, tức là ta chỉ tạo `OrderRepository` chứ **KHÔNG** tạo `OrderItemRepository`

---

## **2. Service _Decomposition_**

Details: [Service decomposition](./ServiceDecomposition.md)
