# `domain`'s **Building _`Blocks`_**

Dùng các `pattern` để tổ chức code. Trong **domains**, cần quản lý:

## `1.` **`Entity` - Thực thể**:

`Entity` là:

- **Identity - `ID`**: đối tượng có **định danh duy nhất - `ID`**
- **Lifecycle**: vòng đời kéo dài
- **Mutable state**: _thuộc tính có thể thay đổi_, nhưng **định danh không đổi**.
  > _Ex_: `Order` có thể có status thay đổi từ **PENDING** thành **SHIPPING** nhưng **luôn** có `ID=123`
- **`ID`-based compare**: `equals()` và `hashCode()` **chỉ dựa trên `ID`** — không so sánh các field khác.

> _**Không dùng `@Setter`** (Lombok) cho Entity — **mọi thay đổi state phải qua `method có tên nghiệp vụ`** (changeEmail, suspend) để đảm bảo `invariant`._

---

## `2.` **`Value Object` - Đối tượng giá trị**:

`Value Object` là object **không có ID**, được định nghĩa hoàn toàn bằng các giá trị bên trong nhó.

> _Ex_ trong `UserEntity` có `ShippingAddress` bao gồm _Số nhà_, _Phường_, _Thành phố_, ... Khi này ShippingAddress là thuộc tính của UserEntity, quan tâm tới giá trị của nó chứ không cấp ID độc lập.

Thường là **immutable**, dùng để mô tả đặc tính.

> _**Lợi ích LỚN NHẤT của `Value Object`** là khả năng **Tự xác thực - `Self-validation`** khi mà ta có thể áp dụng các business rule vào quá trình khởi tạo object thay vì phải check ở nhiều nơi._

Bên cạnh đó, khả năng **tái sử dụng** (dùng chung class Value Object làm field của entity chứ không dùng chung ở mức độ instance) cũng là lợi ích nổi bật của Value Object

---

## `3.` **Domain Events**

`Domain Event` là một **sự kiện có ý nghĩa nghiệp vụ đã xảy ra trong domain**. **Tên** luôn ở **thì quá khứ**: `OrderPlaced`, `PaymentProcessed`, `UserRegistered`.

Dùng để **giao tiếp giữa các Bounded Context** mà **không tạo `tight coupling`**, mô tả 1 hành động cụ thể liên quan tới `aggregate` (`entity`), thường là các sự kiện thay đổi entity.

> Thay vì gọi trực tiếp `PaymentService.charge()` từ `OrderService`, bạn **publish `OrderConfirmed`, `Payment` tự subscribe và thực hiện gửi event.**

---

## `4.` **`Aggregate & Aggregate Root` - Cụm / Gốc cụm**

### `4.1.` **Aggregate Root**:

**`Aggregate Root`** là `Entity` đại diện, đóng vai trò là cổng giao tiếp duy nhất của cả cụm (`Entity` + `sub-entity`).

Phân biệt **Aggregate Root** (Root Entity) và **Child Entity**:

- `Aggregate Root` giống như **Root Entity**:
  - là nơi duy nhất cho phép truy cập tới **Entity** (`Order`) và các **sub-entity** (`OrderItem`) bên trong nó
  - đồng thời kiểm soát **entity's event** và việc **publish event** (`OrderConfirmedEvent`), **pull event**
  - đảm bảo **Business Rule**

  > Điểm dễ nhận biết nhất là **Aggregate Root** là entity có `Repository`

- `Child Entity`: cũng là entity, có ID và có thể thay đổi. Điểm cốt lõi là **_không thể trực tiếp thao tác với nó_** mà **phải thông qua Aggregate Root**
  > _Ex_: không thể tự tạo, xóa `OrderItem` mà phải thao tác với nó qua `Order` : `Order.addItem()`, `Order.removeItem()`, ....

**Quy tắc**: vì **Child Entity** (`OrderItem`) là thành phần (chặt) của **Aggreagate Root** (`Order`), mọi **thao tác với OrderItem** phải được **diễn ra ở Root's Repository**, tức là ta chỉ tạo `OrderRepository` chứ **KHÔNG** tạo `OrderItemRepository`

### `4.2.` **Aggregate**

**`Aggregate`** là cụm các `Entity`, `Sub-Entity`, `Value Object`, `Events`, ... đi liền với nhau để đảm bảo tính **toàn vẹn dữ liệu**.

> _Ex_: Có `Order` là Aggregate Root, trong nó chứa danh sách `OrderItem` (Sub Entity), `ShippingAddress` (Value Object), và `List<OrderPlacedEvent>` (Event)

**`Rule`**: **Transaction Boundary**: một transaction chỉ thay đổi một aggregate. Đây là rule quan trọng nhất trong **Tactical DDD**. Cụ thể:

- Cùng `service` (cùng DB), ta có thể thao tác trên nhiều aggregate cùng lúc cũng ok (nhưng giữ ở mức 2–3 aggregate, không nhiều hơn) - **`pragmatic exception`**
- Khác `service`: bắt buộc áp dụng rule, **tách `transaction`** và sử dụng **Domain Event**

> một `@Transactional` lock càng nhiều bảng, hệ thống càng chậm và càng dễ chết — đặc biệt khi nhiều user thao tác đồng thời

### `4.3.` Phân biệt **Aggregate** và **Aggregate Root**

- **Aggregate Root**: trỏ tới Root Entity
- **Aggregate** trỏ tới cả cụm đó.

---

## `5.` **Repository**

`Repository` cung cấp **abstraction** cho việc **lưu trữ và truy xuất Aggregate**. Domain layer chỉ biết interface — không biết gì về JPA, SQL hay database cụ thể.

> `Repository` chỉ cho **Aggregate Root** — không tạo Repository cho OrderItem hay Address. Muốn lấy OrderItem phải load Order trước, rồi lấy từ aggregate.

---

## `6.` **Domain Service**

`Domain Service` chứa **business logic không tự nhiên thuộc về Entity hay Value Object nào** — thường là logic cần _phối hợp nhiều Aggregate_ hoặc _tính toán phức tạp từ nhiều nguồn_.

**Khi nào dùng Domain Service**:

- **Logic dùng nhiều Aggregate**: Tính giá cần `Order` + `Customer` + `Promotion` cùng lúc.
- **Logic không có `state`**: Chỉ nhận input, trả output — không có field mutable. Pure function về bản chất
- **Logic cần external data**
- **Logic là "_hành động_" trong domain**: "Chuyển tiền" **không thuộc Account nguồn** cũng **không thuộc Account đích** — thuộc `TransferService`.

**Đặc điểm Domain Service**:

- **Stateless**: Không có field mutable.
- **Nằm trong `domain` layer**: Không biết về HTTP, Kafka, JPA. Chỉ biết về domain objects và repository interfaces.
- **Tên là `động từ domain`**: `PricingService`, `TransferService`, `MatchingService` — tên phản ánh **hành động** nghiệp vụ.

> **Dấu hiệu cần Domain Service**: bạn đang cố đặt logic vào Entity nhưng Entity phải `inject Repository` hoặc `biết về Entity khác` để làm được — đó là lúc tách ra Domain Service.
