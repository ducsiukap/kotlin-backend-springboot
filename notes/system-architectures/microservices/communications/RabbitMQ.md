# **_Asynchronous_** communication use **_RabbitMQ_**

---

## **1. `RabbitMQ`** - **Smart** broker - **Dump** Consumer

**Phù hợp với**:

- **Task chạy ngầm**: gửi mail, export ...
- Cần **Complex-Routing**
- Cơ chế **`Retry`** (thử xử **lí lại message** khi xử lí lần đầu bị lỗi) /**`DLQ`** (đẩy **message không xử lý được** vào **queue**) mạnh.

### **1.1. Cấu hình Docker**

tạo [monorepo-microservice-example/**docker-compose.yml**](/codes/monorepo-microservice-example/docker-compose.yml) (root project, nơi chứa cả 2 services).

```yml
# docker-compose.yml

version: "3.8"
services:
  rabbitmq:
    image: rabbitmq:3-management # 3-management: có giao diện web
    ports:
      - "5672:5672" # gửi/nhận message
      - "15672:15672" # web
    environment:
      # username+pass để vào broker
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: secure_password
```

Để chạy container, thực hiện chạy lệnh:

```cmd
docker compose up -d
```

### **1.2. Dependencies & Properties (ở tất cả services)**

- Dependencies: [build.gradle.kts]()
  ```kotlin
  implementation("org.springframework.boot:spring-boot-starter-amqp")
  ```
- Properties:
  - **_Producer_** side:

    ```yaml
    rabbitmq:
      host: localhost
      port: 5672
      username: admin
      password: secure_password
      publisher-confirm-type: correlated # Đảm bỏa message không mất phía producer
      publisher-returns: true # không tìm đc queue -> RabbitMQ trả message ngược về producer
    ```

  - **_Listener_** side:

    ```yaml
    rabbitmq:
      username: admin
      password: secure_password
      host: localhost
      port: 5672
      # listener config
      listener:
        simple: # SimpleMessageListenerContainer
          # ACK mode
          acknowledge-mode: auto
          # retry
          retry:
            enabled: true
            max-retries: 3 # 1 lần chính thức + tối đa 3 lần retires
            max-interval: 10000ms # chờ tối đa 10s cho tín hiệu ACK
            initial-interval: 5000ms # lần đầu, đợi 5s
            multiplier: 1.5 # những lần sau, thời gian đợi x1.5 so với lần trước (backoff strategy)
    ```

---

### **1.3. _`Producer`_ Configuration** :[**user-service**/src/.../infrastructure/config/messagequeue/**RabbitMQConfig.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/config/messagequeue/RabbitMQConfig.java)

- _**`QUEUE`**_: hòm thư // **tương tự topic, nơi lưu các message**
  - **Meaning**: Đại diện cho **consumer** và **mục đích** của consumer => `simple-task`/`simple-command`
  - **Naming-rule**:
    - Main-Queue: `<consumer_service>.<feature>.queue`
    - DLQ: `<consumer_service>.<feature>.queue.dql`

      ex: **_notification.welcome.queue_** and _**notification.welcome.queue.dlq**_

  - **Quantity**:
    - Main-Queue: phụ thuộc consumer service responsibility.

      ex: cả `noti-service` và `abc-service` đều quan tâm tới sự kiện `user.created` => 2 queue.

    - DQL: thông thường `1 Main-Queue` => `1 DLQ`

- _**`Exchange`**_: nơi **routing** message được producer gửi vào Rabbit MQ
  - **Meaning**: Exchange **không giữ message**, nó **routing message tới queue** phù hợp
  - **Naming-rule**:
    - Exchange for **Main-Queue**: `<produce_service>.events.exchange` hoặc `<produce_service>.<type>.exchange`
    - Exchange for **DQL**: `<produce_service>.events.dlx`
  - **Quantity**: _**Đại diện cho `domain`**: mỗi exchange nên ứng với 1 domain_

- **_`Routing key`_**: Key để Exchange quyết định sẽ gửi message tới queue nào
  - **Meaning**: đại diện cho 1 **sự kiện**/**hành động** cụ thể của `domain`
  - **Naming-rule**: `<entity_name>.<action>` (for both DLQ & Main Queue)
  - **Quantity**: mỗi key nên ứng với 1 **action** của domain.

#### **`Exchange` _Types_**:

- `DirectExchange`: routing chính xác dựa trên key
  - Dành cho : **COMMAND** / **EVENT** ĐƠN GIẢN
  - Ý nghĩa: **Gửi đích danh tới đúng `queue` bind với `exchange` này qua `routing-key`**
- `TopicExchange`: routing theo pattern
  - **ĐƯỢC DÙNG NHIỀU NHẤT** .// event-driven
  - **GỬI NHÓM**,
    - ex: `user.*` // **tất cả** `queue` được **_bind_** với `exchange` này mà có `ROUTING KEY MATCH PATTERN` đều nhận được message
    - **THAY THẾ ĐƯỢC DIRECT**
  - **Notes**:
    - nhiều queue cùng 1 key, key matches pattern -> tất cả queue nhận message
    - nhiều queue (của 1 service) với nhiều key, các keys match pattern => tất cả queue cũng nhận message
  - **Pattern**:
    - dấu `.` : ngăn cách cách `segment` // _ex: a.bc.d có 3 segment: a, b, c_
    - '`*`': MATCH 1 SEGMENT
    - '`#`': MATCH 0 hoặc NHIỀU SEGMENT
- `FanoutExchange`: broatcast, không dùng key
  - GỬI TỚI `MỌI QUEUE` được **BIND** với `EXCHANGE`, **bỏ qua** `KEY`
- `HeadersExchange`: routing dựa trên Headers của message thay vì routing

### **1.4. Send Message** - (_**Produce**_ side)

- **Controller**: [**user-service**/src/.../controller/**AuthController.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/interfaces/controller/AuthController.java)
- **Service**: [**user-service**/src/.../service/impl/**AuthServiceImpl.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/application/service/impl/AuthServiceImpl.java)

---

### **1.5. _Consumer_ Configuration**: [**notification-service**/.../config/**RabbitMQConfig.kt**](/codes/monorepo-microservice-example/notification-service/src/main/kotlin/vduczz/notificationservice/config/RabbitMQConfig.kt)

### **1.6. Receive Message** - (**_Consume_** side): **messaging/rabbitmq** Listener: [**notification-service**/src/.../messaging/rabbitmq/**UserCreatedEventListener.kt**](/codes/monorepo-microservice-example/notification-service/src/main/kotlin/vduczz/notificationservice/messaging/rabbitmq/UserCreatedEventListener.kt)

---

## **2. _ACK_, _Retry_ & _DLQ_**

#### **2.1. `ACK`**

Bản chất **`ACK` - Acknowledge** giúp **RabbitMQ** xác định **Consumer** có `consume` thành công hay không.  
Có 3 chế độ **AcknowledgeMode**:

- **`NONE`**: RabbitMQ **đẩy** message tới Consumer là **xong và xóa message**, **không quan tâm** Consume có consume thành công hay không.
- **`MANUAL`**: Phải tự viết code xác nhận:
  - thành công: gọi `channel.basicAck()`
  - thất bại: gọi `channel.basicNack()`

  An toàn nhưng rườm rà.

- **`AUTO`** - **default**:
  - Nếu hàm `@RabbitListener` chạy xong, không gặp lỗi => **Spring tự động bắn `ACK` cho RabbitMQ**
  - Ngược lại, nếu gặp **Exception**, Spring tự động gửi **NACK** (Reject).
    > _Mặc định, message **sẽ quay lại đầu queue** và có thể tạo thành vòng lặp vô hạn._

  => Dùng **AUTO** phải đi kèm cơ chế **Retry** và **DLQ**.

#### **2.2. _Retry_ mechanism**

Thiết lập cơ chế **retry** giúp giới hạn số lần thử.

**Note**: Nếu vượt mức giới hạn, mặc định Spring sẽ **`show error log`** + gửi **`ACK`** xóa thư.

**Config**: (`notification-service`) [application.yaml](/codes/monorepo-microservice-example/notification-service/src/main/resources/application.yaml)

#### **2.3. _DLQ_ mechanism** - Deal Letter Queue

Do mặc định, nếu vượt quá số lần **retries**, mặc định Spring sẽ **log** và **gửi ACK**. Khi này, message sẽ bị xóa => **mất data**.  
**`DLQ` - Dead Letter Queue** sinh ra để làm **container chứa message lỗi**, khắc phục vấn đề trên giúp `Dev` có thể xem xét, fixbugs và chạy lại.

- **_Produce_** side: [**user-service**/src/.../infrastructure/config/messagequeue/**RabbitMQConfig.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/config/messagequeue/RabbitMQConfig.java)
- **Consume** side: [**notification-service**/.../config/**RabbitMQConfig.kt**](/codes/monorepo-microservice-example/notification-service/src/main/kotlin/vduczz/notificationservice/config/RabbitMQConfig.kt)  
  Có thể build service để hứng luôn message của DLQ: [**notification-service**/src/.../messaging/rabbitmq/**UserCreatedEventListener.kt**](/codes/monorepo-microservice-example/notification-service/src/main/kotlin/vduczz/notificationservice/messaging/rabbitmq/UserCreatedEventListener.kt)

## **3. Khi có quá nhiều _`Queue`_ cần _Binding_**

```java

@Configuration
public class RabbitMQConfig {

    // Tạo sẵn 1 cái Exchange dùng chung cho tất cả
    @Bean
    public DirectExchange userEventsExchange() {
        return new DirectExchange("user.events.exchange");
    }
    // Có thể config key trước

    // Cảnh giới gom nhóm: Ném cả rổ Queue và Binding vào 1 Bean duy nhất
    @Bean
    public Declarables amqpDeclarables(DirectExchange userEventsExchange) {

        // Queue 1
        Queue emailQueue = QueueBuilder.durable("notification.welcome_email.queue").build();
        Binding emailBinding = BindingBuilder.bind(emailQueue).to(userEventsExchange).with("user.created");

        // Queue 2
        Queue analyticsQueue = QueueBuilder.durable("analytics.new_user_score.queue").build();
        Binding analyticsBinding = BindingBuilder.bind(analyticsQueue).to(userEventsExchange).with("user.created");

        // Queue 3
        Queue deleteUserQueue = QueueBuilder.durable("cleanup.user_data.queue").build();
        Binding deleteBinding = BindingBuilder.bind(deleteUserQueue).to(userEventsExchange).with("user.deleted");

        // Declarables: Trả về 1 mẻ duy nhất,
        // Spring sẽ tự lôi ra đăng ký hết!
        return new Declarables(
            emailQueue, emailBinding,
            analyticsQueue, analyticsBinding,
            deleteUserQueue, deleteBinding
        );
    }
}
```
