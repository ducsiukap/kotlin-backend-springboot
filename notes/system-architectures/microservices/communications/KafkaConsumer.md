# Kafka **_Producer_** side implement.

## **`1.` Dependencies**

```kotlin
implementation("com.fasterxml.jackson.core:jackson-databind")
// kafka
// implementation("org.springframework.kafka:spring-kafka")
implementation("org.springframework.boot:spring-boot-starter-kafka")
```

## **`2.` Properties**: [notification-service/.../application.yaml](/codes/monorepo-microservice-example/notification-service/src/main/resources/application.yaml)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: earliest
      # earliest: đọc từ đầu nếu chưa có offset
      # latest (default): đọc message mới từ khi start

      group-id: my-service-group

      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      # value-deserializer: org.apache.kafka.common.serialization.StringDeserializer # đọc thành string xong chuyển json sau
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer

      properties:
        # trust all package => cho phép Jackson deserialize từ bất kì package nào
        spring.json.trusted.packages: "*" # hoặc chỉ định các đường dẫn cụ thể

        # Default type for JsonDeserializer
        # spring.json.value.default.type: com.paymentservice.dto.OrderEvent

        # bỏ qua header __TypeId__ được gửi từ producer
        # spring.json.use.type.headers: false

        # type mapping -> xem bên KafkaProducer.md
        # spring.json.type.mapping: >
        #     alias:FQN

    listener:
      # ack-mode
      ack-mode: record
      # record: Commit offset ngay sau khi Listener xử lý xong từng bản ghi.
      # batch: (default) Commit offset sau khi xử lý xong tất cả bản ghi nhận được từ một lần poll()
      # time: Commit offset sau khi xử lý xong bản ghi, miễn là đã quá khoảng thời gian ackTime
      # count: Commit offset khi số lượng bản ghi đã xử lý đạt mức ackCount.
      # manual: Listener chịu trách nhiệm gọi Acknowledgment.acknowledge(), nhưng Spring sẽ gom lại để commit sau.
      # manual_immediate: Gọi acknowledge() phát là Spring gửi lệnh commit lên Kafka ngay lập tức (blocking)

      concurrency: 3 # concurrent consume thread // số thread consumer song song
      poll-timeout: 3000 #
      missing-topics-fatal: true # Fail khi topic không tồn tại
```

## **3. Configuration:** [**notification-service**/src/.../config/**KafkaConsumerConfig.kt**](/codes/monorepo-microservice-example/notification-service/src/main/kotlin/vduczz/notificationservice/config/KafkaConsumerConfig.kt)

Bình thường, `consumer` không cần config gì nhiều (TypeMapper nếu cần, ..).

- **Nếu sử dụng StringDeserializer cho `value-deserializer`**

  ```kotlin
  @Configuration
  class KafkaConsumerConfig {

  // convert received message from String->JSON
  // Dễ lỗi nếu message không phải JSON-type
  @Bean
  fun recordMessageConverter(): RecordMessageConverter {
      return JacksonJsonMessageConverter()
  }
  }
  ```

- **Nếu sử dụng `ack-mode: BATCH`**:

  ```java
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, OrderEvent>
          batchKafkaListenerContainerFactory() {

      ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
          new ConcurrentKafkaListenerContainerFactory<>();

      factory.setConsumerFactory(consumerFactory());
      factory.setBatchListener(true); // BẬT batch mode

      factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

      // Batch size config
      factory.getContainerProperties().setKafkaConsumerProperties(
          propertiesOf(
              ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100",
              ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "50000",  // 50KB tối thiểu
              ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500"   // Đợi tối đa 500ms
          )
      );

      return factory;
  }
  ```

- **Custom `Serializer`/`Deserializer`**:

  ```java
  // Custom Serializer
  public class OrderEventSerializer implements Serializer<OrderEvent> {
      private final ObjectMapper mapper = new ObjectMapper()
          .registerModule(new JavaTimeModule());

      @Override
      public byte[] serialize(String topic, OrderEvent data) {
          if (data == null) return null;
          try {
              return mapper.writeValueAsBytes(data);
          } catch (JsonProcessingException e) {
              throw new SerializationException("Error serializing OrderEvent", e);
          }
      }
  }

  // Custom Deserializer
  public class OrderEventDeserializer implements Deserializer<OrderEvent> {
      private final ObjectMapper mapper = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      @Override
      public OrderEvent deserialize(String topic, byte[] data) {
          if (data == null) return null;
          try {
              return mapper.readValue(data, OrderEvent.class);
          } catch (IOException e) {
              throw new SerializationException("Error deserializing OrderEvent", e);
          }
      }
  }
  ```

- **Generic `Deserializer`**

## **`4.` Receive message (Listener)**: [**notification-service**/src/.../controller/**NotificationController.kt**](/codes/monorepo-microservice-example/notification-service/src/main/kotlin/vduczz/notificationservice/controller/NotificationController.kt)

- ...

  ```java
  @Component
  @Slf4j // log
  public class OrderConsumer {

      // Cơ bản nhất
      @KafkaListener(
          topics = "orders",
          groupId = "order-processor"
      )
      public void consumeOrder(OrderEvent event) {
          log.info("Received order: {}", event.orderId());
          processOrder(event);
      }

      // Với đầy đủ thông tin (message + metadata)
      @KafkaListener(
          topics = "orders",
          groupId = "order-processor"
      )
      public void consumeWithMetadata(
          // Có thể sử dụng @Valid
          @Payload OrderEvent event, // payload

          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
          @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
          @Header(KafkaHeaders.OFFSET) long offset,
          @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,

          ConsumerRecord<String, OrderEvent> record // toàn bộ message
      ) {
          log.info("Received from topic={}, partition={}, offset={}: {}",
              topic, partition, offset, event.orderId());

          // Đọc custom headers
          byte[] correlationIdBytes = record.headers().lastHeader("correlationId").value();
          String correlationId = new String(correlationIdBytes);
      }

      private void processOrder(OrderEvent event) {
          // Business logic
      }
  }
  ```

- **`acks: MANUAL`**

  ```java
  // Với Manual Acknowledgment (xác nhận thủ công)
  @KafkaListener(topics = "orders", groupId = "order-processor")
  public void consumeManualAck(
          ConsumerRecord<String, OrderEvent> record, // message
          Acknowledgment ack // ACK
  ) {
      try {
          processOrder(record.value()); // business

          ack.acknowledge(); // Commit offset SAU khi xử lý thành công
      } catch (RetryableException e) {

          // Không acknowledge → message sẽ được retry
          log.warn("Retryable error, will retry: {}", e.getMessage());
              // có thể dẫn tới infinite retry
      } catch (Exception e) {

          log.error("Fatal error processing order", e);
          ack.acknowledge(); // Vẫn acknowledge để không bị stuck
          // Sau đó xử lý riêng (dead letter queue, alert, v.v.)
      }
  }
  ```

- **`ack-mode: BATCH`**:

  ```java
  @Component
  @Slf4j
  public class BatchOrderConsumer {

      // nhận nhiều message một lần
      @KafkaListener(
          topics = "orders",
          groupId = "batch-processor",
          containerFactory = "batchKafkaListenerContainerFactory"
      )
      public void consumeBatch(List<OrderEvent> events) {
          log.info("Processing batch of {} orders", events.size());
          // Xử lý bulk: lưu DB một lần thay vì N lần
          orderRepository.saveAll(events.stream()
              .map(this::mapToEntity)
              .toList());
      }

      // Batch với ConsumerRecords (đầy đủ metadata)
      @KafkaListener(
          topics = "orders",
          containerFactory = "batchKafkaListenerContainerFactory"
      )
      public void consumeBatchWithRecords(
              List<ConsumerRecord<String, OrderEvent>> records,
              Acknowledgment ack
      ) {
          log.info("Batch size: {}", records.size());
          try {
              processRecords(records);
              ack.acknowledge(); // manual ack
          } catch (Exception e) {
              log.error("Batch processing failed", e);
              // Xử lý từng record riêng lẻ để tìm record lỗi
              ack.nack(0, Duration.ofSeconds(5)); // Retry từ record đầu tiên
          }
      }
  }
  ```

- **Listen from `multiple-topics`**

  ```java
  @KafkaListener(
      topics = {"orders", "order-updates"},
      groupId = "order-group"
  )
  public void consumeMultipleTopics(ConsumerRecord<String, Object> record) {
      switch (record.topic()) {
          case "orders" -> handleNewOrder((OrderEvent) record.value());
          case "order-updates" -> handleOrderUpdate((OrderUpdateEvent) record.value());
      }
  }

  // Pattern matching topics
  @KafkaListener(
      topicPattern = "order.*",  // Regex: orders, order-updates, order-cancelled...
      groupId = "order-group"
  )
  public void consumeTopicPattern(ConsumerRecord<String, Object> record) {
      // Xử lý tất cả topics match pattern
  }
  ```

## **5. Multiple `Message-type` in one `Topic`**

```kotlin
@Component
// Multiple message-type in one topic
@KafkaListener(
    topics = ["user_events"],
    groupId = "notification-group-x",
)
class UserEventListener(
    private val notificationService: NotificationService
) {

    @KafkaHandler // handle 1 loại message type
    fun handleUserCreateEvent(
        @Payload
        request: WelcomeMailRequest, // JSON parse chính xác -> match
    ) {
        notificationService.sendWelcomeMail(request);
    }

    // ____________________ Default Handler ____________________ //
    // khi JSON parse không match request nào
    // bắt buộc hứng để tránh lỗi
    //  => sử dụng cho các message mà nó không quan tâm
    @KafkaHandler(isDefault = true)
    fun handleUnknowMessaage(
        @Payload
        payload: Any, // payload only

        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Header(KafkaHeaders.OFFSET) offset: Long,

        // full-message
        message: ConsumerRecord<String, Any>
    ) {
        // do smth
        println("Hello World :)")
    }
}
```
