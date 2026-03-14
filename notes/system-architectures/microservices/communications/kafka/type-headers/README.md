# **Json `Serialize`/`Deserialize` thông qua `__TypeId__`**

Một số cách triển khai:

- `[Consumer]`: sử dụng **default type deserialize** -> JsonDeserialize luôn deserialize ra cùng 1 type.

  ```yml
  spring:
    kafka:
      consumer:
        properties:
          spring.json.value.default.type: com.example.OrderEvent # chỉ định giá trị mặc định cho deserailze
  ```

  - **Ưu điểm**: Đơn giản, không cần setup thêm.
  - **Nhược điểm**: **toàn bộ message** sẽ luôn được map vào **class** được khai báo là `default-type` -> `consumer chỉ xử lý được duy nhất 1 loại message`
  - **UseCase**: khi service chỉ xử lý 1 loại message. Chính xác hơn là **service chỉ subcribe 1 topic và topic đó chỉ có 1 loại message**.
    > _Dựa trên kiến trúc khuyên nghị của microservice: **`1 topic = 1 loại domain event`**_

- `[Producer]`: cung cấp thông tin về **TypeHeader**, consumer dựa trên nó và sử dụng `TypeMapping` để map đúng type.

  ```yml
  spring.kafka.producer.properties:
    spring.json.add.type.headers: true # default cũng là true nên không cần config như này
  ```

  Khi này, mỗi message sẽ bổ sung **Kafka header** `__TypeID__`, chứa **FQN** (fully-qualified class name - đường dẫn chi tiết tới class đó tại producer) hoặc **alias** nếu produce cũng sử dụng **TypeMapper**.

  Các cách **config `TypeMapper`**:
  - Config trong `application.yml`: (**_`UNRECOMMENDE`_**)

    ```yml
    spring.kafka.consumer.properties:
      # nếu 1 type mapping
      # định dạng: alias:FQN
      spring.json.type.mapping: "order:com.producer.service.events.OrderEvent"

      # nếu có nhiều type mapping
      spring.json.type.mapping: >
        alias1:FQN1,
        alias2:FQN2,
        ...
    ```

    Rõ ràng, cách này **tương đối dễ sai**.

  - Config bằng code: (**_`RECOMMENDED`_**)
    Xem chi tiết ở phần [Configuration](#configuration)

- [**producer** + **consumer**]: **`Shared Library`**: cả 2 service - `producer` và `consumer` - đều cần cài đặt chung thư viện chứa định nghĩa của `message` đó

---

## **Tài liệu này hướng dẫn sử dụng `Serialize`/`Deserialize` kết hợp TypeMapping**

Vấn đề: **Bên nào cần mapping?**
![Who should declare Type Mapping?](./kafka_mapper_sides.png)

Cá nhân mình thấy cả 2 đều dùng TypeMapping để sử dụng chung `alias` hợp lí hơn

**Đổi tên mặc định `__TypeId__` của Spring**: [Chi tiết](#1-configuration) 

```java
typeMapper.setClassIdFieldName("x-event-type");
```

---

## **`1.` Configuration**

- **[Producer](./config/Producer.md) configuration**
- **[Consumer](./config/Consumer.md) configuration**

## **`2.` _Send_ and _Listen_ event implementation**

#### **_`2.1` [Producer](./impl/Producer.md)_**

#### _**`2.2.` [Consumer](./impl/Consumer.md)**_
