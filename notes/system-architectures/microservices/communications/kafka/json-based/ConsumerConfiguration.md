# Consumer `JSON Deserializer` configuration

```yml
spring:
  kafka:
    bootstrap-servers: localhost:9094
    consumer:
      auto-offset-reset: earliest
      # earliest: đọc từ đầu nếu chưa có offset
      # latest (default): đọc message mới từ khi start

      # group-id
      group-id: notification-service-group

      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer

      # value deserializer: JacksonJsonDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JacksonJsonDeserializer

    properties:
      # trust all package: "*"
      # => cho phép Jackson deserialize từ bất kì package nào
      spring.json.trusted.packages: "*"
```

Vấn đề của `Consumer` là `deserializer` đúng **class**
dựa trên **type-headers** (`__TypeId__`), **json-default-value**, ....

**Solutions**:

- (**consumer**) `VALUE_DEFAULT_TYPE`: luôn deserializer ra 1 class

  ```yml
  spring:
    kafka:
      consumer:
        properties:
          spring.json.value.default.type: com.example.OrderEvent
  ```

  - **Ưu điểm**: Đơn giản, không cần setup thêm.
  - **Nhược điểm**: **toàn bộ message** sẽ luôn được map vào **class** được khai báo là `default-type` -> `consumer chỉ xử lý được duy nhất 1 loại message`
  - **UseCase**: khi service chỉ xử lý 1 loại message. Chính xác hơn là **service chỉ subcribe 1 topic và topic đó chỉ có 1 loại message**.
    > _Dựa trên kiến trúc khuyên nghị của microservice: **`1 topic = 1 loại domain event`**_

- (**producer**) `ADD_TYPE_INFO_HEADERS`: producer cung cấp type headers

  ```yml
  spring:
    kafka:
      producer:
        properties:
          spring.json.add.type.headers: true # default cũng là true
  ```

  Khi này, mỗi message sẽ bổ sung **Kafka header** `__TypeID__`, chứa **FQN** (fully-qualified class name - đường dẫn chi tiết tới class đó tại producer) .

  Phía `consumer`, mặc định cũng sử dụng thông tin từ type headers, dùng chính FQN được cung cấp để xác định class

  ```yml
  spring:
    kafka:
      consumer:
        properties:
          spring.json.use.type.info.headers: true #  default
  ```

  > _Dù **linh hoạt hơn** việc dùng `default-type` nhưng mà tóm lại là **rất đần** vì `FQN ở service khác nhau, đầu thể match`?_

- (**producer** + **consumer**) `TypeMapping`: giải pháp cho nhược điểm của cách bên trên.

  Thay vì sử dụng FQN cho `__TypeId__`, nếu ta cung cấp **TypeMapper**, `alias` của type đó sẽ được truyền vào `__TypeId__`
  - **Config trong application.yml** (`unrecommended`)

    ```yml
    # producer
    spring:
    kafka:
      producer:
        properties:
          # nếu 1 type mapping
          spring.json.type.mapping: "order:com.producer.service.events.OrderEvent"
          # định dạng: alias:FQN

    pring:
    kafka:
      producer:
        properties:
          # nếu nhiều type mapping
          spring.json.type.mapping: >
            "order:com.consumer.service.events.OrderEvent",
            "notification:com.consumer.service.events.NotificationEvent"
    ```

  - **Config bằng code** (**_`RECOMMENDED`_**)

    **`Producer`**: Xem `TypeMapping` config trong [../advanced-config/Producer](../advanced-config/Producer.md)

    **`Customer`**: Xem `TypeMapping` consumer side config trong [../advanced-config/Consumer.md](../advanced-config/Consumer.md)

- (**producer** + **consumer**): **`Shared Library`**: cả 2 service - `producer` và `consumer` - đều cần cài đặt chung thư viện chứa định nghĩa của `message` đó
