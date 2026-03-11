# Kafka **_Producer_** side implement.

## **`1.` Dependencies**

```kotlin
implementation("com.fasterxml.jackson.core:jackson-databind")
// kafka
// implementation("org.springframework.kafka:spring-kafka")
implementation("org.springframework.boot:spring-boot-starter-kafka")
```

## **`2.` Properties** : [user-service/.../application.yaml](/codes/monorepo-microservice-example/user-service/src/main/resources/application.yaml)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092

    # --- PRODUCER CONFIG ---
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer # Java Object -> JSON
      acks: all # Đợi tất cả replicas xác nhận
      retries: 3 # Tự retry khi fail
      batch-size: 16384 # Gom batch 16KB trước khi gửi -> tăng hiệu suất
      linger-ms: 10 # 1 batch tồn tại tối đa 10ms trước khi được gửi (kể cả chưa đầy batch)
      compression-type: snappy # Nén message (snappy/gzip/lz4)
      properties:
        enable.idempotence: true # Tránh duplicate message
        spring.json.add.type.headers:
          true # tận dụng JsonDeserialize
          # tuy nhiên, để làm được điều này, Xem bên dưới!
```

**Để `JsonDeserialization` có thể thực hiện được ở phía `consumer`, có một số cách**:

- **Shared Library**: cả 2 service - `producer` và `consumer` - đều cần cài đặt chung thư viện chứa định nghĩa của `message` đó
- **Phía `Consumer` chỉ định `default-type`**:
  ```yaml
  properties:
    spring.json.value.default.type: com.example.dto.OrderEvent
  ```
  Chỉ phù hợp khi `consumer` chỉ lắng nghe trên **MỘT TOPIC** và **Topic đó có DUY NHẤT 1 TYPE**
- Sử dụng `StringDeserialize` + `MessageConverter` ở phía **Consumer**:

  ```yaml
  consumer: # deserialize ở phía consumer
    value-deserializer: org.apache.kafka.common.serialization.StringDeserializer # StringDeserialize
  ```

  ```kotlin
  // @Configuration config/KafkaConsumerConfig.kt

  @Bean  // RecordMessageConverter
    fun recordMessageConverter(): RecordMessageConverter {
        return JacksonJsonMessageConverter()
    }
  ```

- **Type-Mapping**

  - Mapping definition trong `application.yaml`:

    ```yaml
    properties:
      spring.json.type.mapping: >
        order-event:com.paymentservice.dto.OrderEvent,
        payment-event:com.paymentservice.dto.PaymentEvent,
        notif-event:com.paymentservice.dto.NotificationEvent

    # syntax: alias:FQN
    # (Fully Qualified Name - tên đầy đủ com.domain.packages....ClassName)

    # Header gửi đi: __TypeId__ = "order-event"
    ```

  - Mapping definition bằng code: (**có vẻ ổn hơn?**)
    - `Producer` side:

      ```java
      // KafkaProducerConfig.java

      @Bean
      public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {

        // init serializer
        JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>();

        // set type-mapper
        serializer.setTypeMapper(typeMapper());
        serializer.setAddTypeInfo(true); // enable type header

        return new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(), // producer default config (application.yaml)
                // nếu có config riêng cho producer thì truyền vào

                new StringSerializer(), // key serializer
                serializer // value serializer
        );
      }

      @Bean
      public DefaultJacksonJavaTypeMapper typeMapper() {

        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper(); // init TypeMapper

        // Ưu tiên header hơn default type
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.TYPE_ID);

        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put(
                KafkaTopicConstrains.USER_CREATED_TOPIC,
                WelcomeMailRequest.class
        );
        // put more ...

        // set mapping __TypeID__
        typeMapper.setIdClassMapping(mappings);
        return typeMapper;
      }
      ```

    - `Customer` side:

      ```java
      // config/KafkaCustomerConfig.java

      // config tương tự producer
      // customerFactory() -> deserializer
      // typeMapper()
      ```

  **Bên nào nên định nghĩa TypeMapping?**
  ![Who should declare Type Mapping?](kafka_mapper_sides.png)

## **`3.` Configuration**: [**users-service**/src/.../infrastructure/config/messagequeue/kafka/**KafkaProducerConfig.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/config/messagequeue/kafka/KafkaProducerConfig.java)

> **NOTES**: NHỮNG THỨ ĐƯỢC CONFIG BỞI `producerFactory`, `consumerFactory` MÀ CÓ TRONG `application.yaml` THÌ CHỈ ĐƯỢC CONFIG Ở 1 TRONG 2, **KHÔNG ĐƯỢC CONFIG Ở CẢ 2 PHÍA**

```java
// Tạo 1 topic tự động
// có partition và replication
@Bean
public NewTopic userEventsTopic() {
    return TopicBuilder
            .name(KafkaTopicConstrains.USER_CREATED_TOPIC)
            .partitions(3) // 3 partition -> parallel processing
            .replicas(1) // 1 replica
            .build();
}

// tạo nhiều topic cùng lúc
@Bean
public KafkaAdmin.NewTopics allTopics() {
    return new KafkaAdmin.NewTopics(
            TopicBuilder // Topic #1
                .name("orders")
                .partitions(3)
                .replicas(1)
                .build(),

            TopicBuilder // Topic #2
                .name("payments")
                .partitions(3)
                .replicas(1)
                .build(),

            TopicBuilder // Topic #3
                .name("notifications")
                .partitions(1)
                .replicas(1)
                .build()
    );
}
```

#### **`Topic` _Naming Rules_**

- có thể sử dụng `.`, `-`, `_` làm dấu phân cách.

- **_`Multi-Topic`_**: **`One Message-Type` (One `Event`) per `Topic`**

  ```plaintext
  <environment>.<domain>.<classification>.<description>.<version>
  ```

  Trong đó:
  - `<enviroment>`: `dev`, `staging`, `prod` -> giúp tách biệt dữ liệu giữa các môi trường nếu dùng chung Cluster
  - `<domain>`: entites -> `order`, `payment`, ...
  - `<classification>`: loại dữ liệu -> `events`, `commands`, `fct` (fact), `cdc` (change data capture), ...
  - `<description>`: sumary -> `created`, `status-changed`, ...
  - `<version>`: version của **schema** -> **RẤT QUAN TRỌNG** khi thay đổi **cấu trúc message** mà không muốn làm sập các consumer cũ

  Example: `log.<service-name>.<level>`(**Logging**, ex: _log.user.error_), `<domain>.<entity>.<action>` / `<domain>.<classification>.<event>`, (**Entity Events**, ex: _user.events.created_), `<cdc>.<db-name>.<table-name>`( **CDC**, ex: _cdc.inventory.products_), `<original-topic>.retry` (**Internal/Retry**)

  > _Dùng **nhiều topic** khi các loại **sự kiện không liên quan đến nhau về mặt thứ tự** và có **các nhóm Consumer khác hẳn nhau** hoặc cần **thông lượng lớn** / **consumer scale khác nhau**_

- **_`Single-Topic`_**: **`Multiple Message-Types` in `One-Topic`**:
  - Phân biệt **Message**:
    - **Producer Type Header**: `__TypeId__ = user.created.v1`
    - **Custom Header**: `event-type: user.created.v1`
    - **Payload**: `{eventType: user.created.v1, ...}`
  - **Naming Rule**:
    - **Topic**: `<env>.<domain>.<entity>.events` (nếu domain có nhiều entity) / `<env>.<domain>.events` (nếu domain chính là entity) (linh hoạt bỏ/giữ `<env>`)
    - **Event Type**: `<entity>.<action>`
    - **Message Key**: thường là `entityId`
  - **Tại sao phải có Naming Rule cho cả `Message Key`?**
    - Trong mô hình gộp nhiều event vào 1 topic, cái Key là "linh hồn" của việc đặt tên.
    - `Quy tắc`: Tất cả các Event-Type trong cùng một Topic phải dùng chung một loại Key (ví dụ đều dùng orderId).
    - `Tại sao`: Để đảm bảo tất cả các sự kiện của cùng một đơn hàng ORD-123 (từ lúc tạo đến lúc hủy) đều nhảy vào cùng một Partition. Nếu ông đổi Key (lúc dùng orderId, lúc dùng customerId), thứ tự xử lý sẽ loạn hết cả lên.

      > Kafka dùng key để quyết định partition: `partition = hash(key) % number_of_partitions`

  - **Ưu điểm**:
    - giảm số lượng Topics cần quản lý
    - **đảm bảo thứ tự** (do Kafka chỉ đảm bảo thứ tự trong cùng Partition)
  - **Nhược điểm**:
    - Network Bandwidth: vì phải xử lý toàn bộ message từ topic, kể cả loại nó không quan tâm
    - CPU & Deserialization
    - Lag & Offset: các event không quan tâm chiếm số lượng quá lớn
  - **Sử dụng khi**:
    - Cần ưu tiên thứ tự.
    - Lưu lượng vừa phải

## **`4.` _Asynchronous_ messaging:**

- **Controller**: [**user-service**/src/.../controller/**AuthController.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/interfaces/controller/AuthController.java)
- **Service**: [**user-service**/src/.../service/impl/**AuthServiceImpl.java**](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/application/service/impl/AuthServiceImpl.java)

```java
// example kafka messaging use-case
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducerService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private static final String TOPIC = "orders";

    // --- Gửi đơn giản (fire-and-forget) ---
    public void sendOrder(OrderEvent event) {
        kafkaTemplate.send(TOPIC, event.orderId(), event);
        // Key = orderId → đảm bảo cùng order luôn vào cùng partition
    }

    // --- Gửi và xử lý kết quả async ---
    public void sendOrderAsync(OrderEvent event) {
        CompletableFuture<SendResult<String, OrderEvent>> future =
            kafkaTemplate.send(TOPIC, event.orderId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send order {}: {}", event.orderId(), ex.getMessage());
                // Xử lý failure: retry, dead letter queue, v.v.
            } else {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Sent order {} to partition {} at offset {}",
                    event.orderId(),
                    metadata.partition(),
                    metadata.offset()
                );
            }
        });
    }

    // --- Gửi với custom Headers ---
    public void sendOrderWithHeaders(OrderEvent event, String correlationId) {
        ProducerRecord<String, OrderEvent> record = new ProducerRecord<>(
            TOPIC, null, System.currentTimeMillis(),
            event.orderId(), event
        );
        record.headers()
              .add("correlationId", correlationId.getBytes())
              .add("source", "order-service".getBytes())
              .add("version", "v2".getBytes());

        kafkaTemplate.send(record);
    }

    // --- Gửi đến partition cụ thể ---
    public void sendToSpecificPartition(OrderEvent event, int partition) {
        kafkaTemplate.send(TOPIC, partition, event.orderId(), event);
    }
}
```

## **`(OPTIONAL)` Advanced Kafka Producer Configuration**

```java
@Configuration
public class KafkaProducerConfig {

  @Bean
  public ProducerFactory<String, OrderEvent> producerFactory() {
      Map<String, Object> config = new HashMap<>();

      config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
      config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

      // Idempotent producer (tránh gửi duplicate)
      config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
      config.put(ProducerConfig.ACKS_CONFIG, "all");
      config.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
      config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

      // Performance tuning
      config.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);     // 32KB batch
      config.put(ProducerConfig.LINGER_MS_CONFIG, 20);          // Đợi 20ms gom batch
      config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB buffer
      config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

      return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<String, OrderEvent> kafkaTemplate() {
      KafkaTemplate<String, OrderEvent> template =
          new KafkaTemplate<>(producerFactory());

      // Global callback cho tất cả messages của template này
      template.setProducerListener(new ProducerListener<>() {
          @Override
          public void onSuccess(ProducerRecord<String, OrderEvent> record, RecordMetadata metadata) {
              log.debug("Message sent successfully: topic={}, partition={}, offset={}",
              metadata.topic(), metadata.partition(), metadata.offset());
          }

          @Override
          public void onError(ProducerRecord<String, OrderEvent> record, RecordMetadata metadata, Exception exception) {
              log.error("Failed to send message: key={}, error={}",
              record.key(), exception.getMessage());
          }
      });

      return template;
  }
}
```
