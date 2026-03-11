# _**Asynchronous**_ communication

## **1. _Bản Chất_**

**`Asynchronous`** communication / **`Event-Driven`** architecture

- Sử dụng **Message Broker**
- hoặc, sử dụng **Event Streaming**

#### **_Cơ chế kỹ thuật_**

Thay vì 2 services trực tiếp nói chuyên với nhau `point-to-point` (qua REST APIs / gRPC, ...),  
**`Asynchronous`** giao tiếp qua một **trung gian chuyển tiếp nhanh**, gọi là **_`Message Broker`_** như **Kafka**, **RabbitMQ**, **ActiveMQ**, ... Cụ thể:

- **Service A** - `Producer` (Người phát tin / Nhà cung cấp / Người xuất bản, ...): thay vì gửi request tới **Service B**, _**A**_ pack request vào trong một thông báo (**Message**/**Event**) và phát vào trung gian **`Broker`**.
- **Service B** - `Consumer` (Người nhận tin / Người tiêu thụ): đăng ký **theo dõi** **`Broker`**.  
  Khi có **Event**, Broker tự gửi thông báo tới _**B**_. **B** nhận được event và xử lý như nhận được HTTP Request từ _**A**_

```plaintext
Producer -> Broker -> Consumers
```

#### _**`Advantages`**_

- **Loose Coupling**: Khắc phục được vấn đề **Temporal Coupling** của cơ chế **_Synchronous_**.  
  _**A**_ không cần biết _**B**_ có tồn tại không. Kể cả khi _**B**_ sập:
  - _**A**_ vẫn có thể **_phục vụ_** được client _**bình thường**_ => Tăng **_`Upstream`_**
  - Khi **_B_** sống lại, tự lấy message cũ ra và thực hiện bù (hoặc không).

- **Scalable** - Khả năng mở rộng: Khi thêm `services`, ta **KHÔNG CẦN** sửa ở **_`A`_**, chỉ cần cho các service mới **_subcribe_** vào **Broker** để lắng nghe _**Event**_ từ A.

#### _**`Disadvantages`**_

- **Eventual Consistency** - Tính **_`Nhất quán`_**: vấn đề **đồng bộ dữ liệu** giữa các services.
- **Ops**: cần nuôi thêm server chạy **Kafka**/**RabbidMQ**  
  Hệ thống **phụ thuộc** vào **MessageBroker** -> nếu Broker lỗi, các service không giao tiếp được với nhau.
- **Traceability** - **_`Truy vết`_** lỗi: **Debug** rất khó khăn vì luồng chạy bị đứt đoạn. Phải dùng `Correlation ID` để xem message lỗi ở đâu.

---

## **2. _Roadmap_**

- **RabbitMQ**: [RabbitMQ.md](./RabbitMQ.md)
- **Kafka**: [Kafka.md](./Kafka.md)