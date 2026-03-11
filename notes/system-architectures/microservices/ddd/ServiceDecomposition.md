# Service **_Decomposition_**

**Service Decomposition** là cách **băm** một cấu trúc nguyên khối - **Monolith** - thành các **Microservice** nhỏ gọn.

Vấn đề **`Distributed Monolith`** - **Nguyên khối phân tán**: các service bị tách không hợp lí dẫn tới **Tight Coupling** - phụ thuộc chặt trẽ - _**khi mà một service bị lỗi, kéo theo service phụ thuộc cũng lỗi theo**_ và làm **độ trễ mạng** - `network latency` - của hệ thống tăng lên.

### **1. Decompose by _Business Capability_** - cắt theo chức năng nghiệp vụ

Service được chia theo sơ đồ tổ chức của công ty hoặc các phòng ban nghiệp vụ.

- `CustomerService`: dành cho Chăm sóc khách hàng department
- `AccountingService`: dành cho Kế toán department
- `InventoryService`
- ...

**Ưu điểm**: Dễ hiểu với `non-tech`.

**Nhược điểm**: Dễ bi chồng chéo:

- **Khách hàng** của **Kế toán** quan tâm tới công nợ, tài chính, ...
- **Chăm sóc khách hàng** quan tâm tới lịch sử khiếu nại, giải đáp, ..

  > _Khi đưa tất cả vào `CustomerService` làm nó bị phình to - **Spaghetti Code**._

### **2. Decompose by _Subdomain_ / _Bounded Context_ - _DDD_**

#### _`2.1.` Phân rã theo **`Subdomain`** : **Không gian bài toán - Problem Space**_

Trước khi code, phải nhìn hệ thống dưới góc độ **business**: Toàn bộ doanh nghiệp là một **Domain**.  
Nhưng khi **Domain** phình to, khó có thể quản lí cùng lúc. Khi này, phải chia nó ra thành **SubDomain**.

**`Types of SubDomain`**

- **_Core_ Subdomain** - Nghiệp vụ **cốt lõi**: đây là đặc trưng giúp hệ thống khác biệt, cạnh tranh với các hệ thống khác, cần tối ưu tốt.

  Ex: Thuật toán gợi ý sản phẩm riêng biệt,...

- **_Supporting_ Subdomain** - **Hỗ trợ**: là nghiệp vụ cần thiết để hệ thống chạy, không mang lại lợi ích cạnh tranh cốt lõi.

  Ex: Hệ thống quản lý kho hàng, quản lý danh mục sản phẩm, ...

- **_Generic_ Subdomain**: là những bài toán mà hệ thống nào cũng giống nhau.

  Ex: Hệ thống thanh toán, Hệ thống gửi Email/SMS, Xác thực người dùng

  > _**Generic Subdomain** có thể mua từ `SaaS` bên ngoài_

#### _`2.2.` Định hành **Bounded Context**_

Khi có `Subdomain`, cần bắt đầu vẽ các **Bounded Context** bao quanh chúng, định nghĩa rõ ràng: Trong subdomain này, mọi thuật ngữ, classs, logic chỉ **mang tính duy nhất**

> _Việc dùng chung class cho nhiều Context dễ dẫn tới class bị **phình quá to**, chứa nhiều thuộc tính và **tightly coupling** khi thay đổi logic của Context A làm hỏng logic của Context B._

Vì vậy, **Bounded Context** sinh ra để quy định: _**Mỗi context tự định nghĩa class riêng (nếu dùng chung 1 entity), với `database` riêng của nó**_

#### _`2.3.` Mapping from **Bounded Context** to **Microservice**_

**Rule**: `1 Bounded Context` = `1 Microservice` (hoặc một vài service rất nhỏ bên trong nó).

Ví dụ:

- **Identity Context** -> **`UserService`** chịu trách nhiệm về mọi thao tác liên quan tới User, quản lý thông tin tài khoản, ... (`Generic Subdomain`).
- **Communication**/**Notification Context** -> **`NotificationService`**: chịu trách nhiệm kết nối Mail Server, quản lí template email, gửi SMS, ... (`Generic Subdomain`).
