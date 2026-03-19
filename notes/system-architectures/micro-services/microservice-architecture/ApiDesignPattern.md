# **_`API` Design_ Pattern**

## **`1.` `Synchronous` vs `Asynchonous`**

Việc lựa chọn giữa `sync` và `async` là lựa chọn đầu tiên, và **quan trọng nhất** khi thiết kế **giao tiếp** giữa các services. Quyết định này ảnh hưởng trực tiếp đến `resilience`, `coupling` và `performance`.

- **Synchronous - `REST`/`gRPC`:** Client gửi request và chờ response. Biết ngay kết quả thành công hay thất bại.
  > _**Dùng khi**: cần kết quả ngay để tiếp tục — auth check, query data, tính giá._
- **Asynchronous - `Event`/`Message Queue`:** Publisher gửi event và không chờ. Consumer xử lý khi sẵn sàng. Kết quả nhận sau.
  > _**Dùng khi**: không cần kết quả ngay — gửi email, trừ tồn kho, tạo invoice._

**Rule of thumb**: nếu user có thể nhận response "**`đang xử lý`**" thay vì "**`xong rồi`**" → dùng `async`. Nếu user **cần biết kết quả ngay lập tức** → dùng `sync`.

---

## **`2.` `REST`**

`REST` vẫn là **lựa chọn mặc định** cho `external API` và **hầu hết** `internal API`. Thiết kế chuẩn từ đầu giúp tránh nhiều vấn đề về versioning và client coupling sau này.

Syntax: `[noun]s` + **HTTP Method**

Ex:
| **HTTP Method** | **API** | **Meaning** | |
| :-: | :-: | :-:| :-:|
| `GET` | `/users` | Get all orders | `idempotent`, `cacheable`, `no side effect` |
| `GET` | `/users/{id}` | Get user by ID | `idempotent`, `cacheable`, `no side effect` |
| `POST` | `/users` | Create new user | `không idempotent` |
| `PUT` | `/users/{id}` | Fully Update an user by id | `idempotent` |
| `PATCH` | `/users/{id}` | Update partial an user | |
| `DELETE` | `/users/{id}` | Delete an user | `idempotent` |

---

## **`3.` `gRPC`**

`gRPC` dùng **Protocol Buffers** (`binary`) thay vì JSON — nhanh hơn 5–10x, `type-safe`, `contract-first`. Lý tưởng cho **internal communication** giữa các microservices cần performance cao.

---

## **`4.` `Event` / Async**

Thay vì gọi trực tiếp, service **publish event** lên `Kafka`/`RabbitMQ`. Consumer xử lý khi sẵn sàng. Hoàn toàn decoupled về thời gian — **publisher không cần biết consumer có đang chạy không**.

**Event Naming**: `past tense`

Ex: `PaymentProcessed`, `NotificationSent`, `InventoryReserved`, `ShipmentCreated`, ...

## **`5.` API `versioning`**

**Breaking change** là điều không thể tránh. `Versioning` đúng cách **giúp client cũ không bị vỡ khi API thay đổi**.

Đơn giản nhất: `/api/version/users` (**Khuyên dùng**)

Ex: `/api/v1/users`, `/api/v2/users`, ...

> _**Note**: Không bao giờ xóa version cũ đột ngột. Thông báo deprecation ít nhất 6 tháng trước. Dùng Sunset header để client tự động nhận cảnh báo._
