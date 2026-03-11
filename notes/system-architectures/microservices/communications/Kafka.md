# _**Asynchronous**_ communication use _**Kafka**_

## **`1.` `Kafka`** - **_Dump_** broker - **_Smart_** Consumer

Khác với **RabbitMQ** là **Message Queue** / **Message Broker**, `Kafka` là **event streaming platform**.

|   Tiêu chí   |         **Kafka**         |         **RabbitMQ**         |
| :----------: | :-----------------------: | :--------------------------: |
| `Throughput` | Cực cao (`triệu msg / s`) | Trung bình (`nghìn msg / s`) |
|   Lưu trữ    | Lâu dài (`configurable`)  |    Xóa sau khi `consume`     |
|    Replay    |   Có - `rewind offset`    |            Không             |
|   Ordering   | Đảm bảo trong `partition` |    Đảm bảo trong `queue`     |
| **Use case** |   Event streaming, log    |       RPC, task queue        |

---

## **_SHOULD NOT_ use Kafka Use case**

- **Simple `RPC` request/reply** -> `REST` / `gRPC`
- Message queue **nhỏ**, **ít traffic** -> `RabbitMQ`
- Cần **Complex Routing** với exchange -> `RabbitMQ`

---

## **Kafka's _Components_**:

- **`Broker`**: **Kafka server**, lưu: `topic`, `partition` và `message log`

- **`Topic`**:
  - Tất cả `event` sẽ được **ghi nối tiếp nhau** vào một **Topic**, ex: topic `user-events`

  - Điểm **ĐẶC BIỆT** của `kafka` là: **Đọc xong không mất data**, data vẫn nằm đó cho tới khi **hết hạn** (`Retention` policy, mặc định 7 ngày)

- **`Partition`**:
  - `Kafka` chia **Topic** ra thành nhiều **Partition** (0, 1, 2, ...).
  - Nhờ vậy, có thể gắn **`N Broker Server`** để **đọc/ghi song song**. Đây là điểm **cốt lõi** có thể giúp `kafka` đặt tới `triệu msg/s`

- **`Offset`** - Marker:
  - Vì không xóa message sau khi đọc, `Kafka` cung cấp cơ chế **Offset** là số thứ tự 0, 1, 2, ... (số thứ tự của `last message`) giúp **consumer** có thể xác định nó **đã đọc tới đâu**.

  - Cách lưu:

    ```text
    offset 0 -> message1
    offset 1 -> message2
    ...
    ```

- **`Consumer Group`**:
  - Giả sử có `3 instance` của **NotificationService** chạy để chia tải. Ta cần gom 3 instance vào chung `groupId="notification-group"`.
  - Sau đó, `Kafka` sẽ chia **Partition** cho 3 instance đọc => **Đảm bảo chỉ 1 instance trong group đọc được và thực hiện task**

    _**Ví dụ**_: Khi có **6 Partions** và **3 Consumers**, mỗi consumer sẽ được chia **2 Parttions**. Nếu có **>6 Consumers**, khi này, chỉ có **6 Consumers** hoạt động, các consumer còn lại **idle**.

  - **Điều kiện để Scale**: `Max số consumer hoạt động song song` = `số Partion`

```plaintext
┌─────────────────────────────────────────────────────────────┐
│                        Kafka Cluster                        │
│                                                             │
│  ┌──────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │  Broker 1    │    │  Broker 2   │    │  Broker 3   │     │
│  │───────────── │    │─────────────│    │─────────────│     │
│  │ Topic: orders│    │Topic: orders│    │Topic: orders│     │
│  │ Partition 0  │    │ Partition 1 │    │ Partition 2 │     │
│  │ (Leader)     │    │ (Leader)    │    │ (Leader)    │     │
│  │ Partition 1  │    │ Partition 0 │    │ Partition 1 │     │
│  │ (Follower)   │    │ (Follower)  │    │ (Follower)  │     │
│  └──────────────┘    └─────────────┘    └─────────────┘     │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              ZooKeeper / KRaft (mới)                 │   │
│  │         (Quản lý metadata, leader election)          │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
        ↑ produce                             ↓ consume
┌──────────────┐                    ┌──────────────────────┐
│   Producer   │                    │  Consumer Group A    │
│ (Spring Boot)│                    │  Consumer 1, 2, 3    │
└──────────────┘                    └──────────────────────┘
```

## **`2.` Triển khai _`Kafka`_** : [Implement Kafka](./KafkaImplementation.md)
