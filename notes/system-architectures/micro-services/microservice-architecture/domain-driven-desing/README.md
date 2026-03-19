# _**Domain-Driven**_ Design - `DDD`

## **`1.` _DDD_**

**DDD** là **triết lí thiết kế** xoay quanh **nghiệp vụ cốt lõi** - `core business logic` / `domain` - được chia thành 2 phần chính **Strategic** (_Chiến lược_) và **Tactical Design** (_Chiến thuật_)

### `1.1.` **_Strategic_** design - Thiết kế chiến lược: **Ubiquitous Language** & **Bounded Context**

#### `1.1.1.` **`Ubiquitous Language`** - _Ngôn ngữ chung_: **Developers** & **Business Team** bắt buộc phải dùng chung **bộ từ vựng**.

> Cụ thể, nếu Business gọi là "Khách hàng", trong code **KHÔNG ĐƯỢC** tự ý đổi thành **_Account_** hay **_User_**

#### `1.1.2.` **`Bounded Context` - Ngữ cảnh giới hạn**: là **cách tốt nhất** / **quan trọng nhất** để chia nhỏ hệ thống thành các `microservices`.

> Cụ thể, với **cùng một thực thể**, đặt ở **ngữ cảnh khác nhau** thì có **_ý nghĩa và thuộc tính hoàn toàn khác nhau_**

Ex: khái niệm **Product**:

- **Inventory Context** - ngữ cảnh kho hàng: quan tâm tới trọng lượng, vị trí, số lượng, ...
- **Sales Context** - ngữ cảnh kinh doanh: quan tâm giá bán, hình ảnh, mô tả

Vì vậy, nên tách chúng thành 2 service độc lập, thay vì tạo 1 bảng `Product` khổng lồ với quá nhiều thuộc tính.

---

### `1.2.` **_Tactical_** design - Thiết kế chiến thuật: [**Building _`Blocks`_**](./BuildingBlocks.md)

---

### **Note**: DDD không phải `silver bullet` — chỉ nên áp dụng cho **`core domain`** (phần tạo ra giá trị cạnh tranh). Các `subdomain` đơn giản (gửi email, upload file) **không cần DDD đầy đủ**.

---

## **2. Service _Decomposition_**

Details: [Service decomposition](./ServiceDecomposition.md)
