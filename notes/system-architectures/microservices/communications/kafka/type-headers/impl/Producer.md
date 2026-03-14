# **Producer implementation theo _DDD_**

## _`KafkaTemplate.send()`_:

- Với `StringSerialize`: ta cần map Object -> String trước khi gửi
  - Sử dụng: `KafkaTemplate<String, String>`
  - Phải tự thêm header.
  - Chi tiết: [StringSerialize](#sau-khi-thêm-event-vào-outbox-relay-sẽ-thực-hiện-quét-định-kì-và-gửi-event-infrastructurerelaykafkaoutboxrelayjava)
- Với `JsonSerialize`: ta gửi trực tiếp object vào hàm `send()`
  - Sử dụng: `KafkaTemplate<String, Object>`
  - Không cần thêm header, gửi trực tiếp object vào `send()`, Spring tự thêm TypeHeader (hoặc alias nếu có TypeMapping).
  - Chi tiết: [JsonSerialize](#bean-implementations-infrastructuremessagingkafkaeventpublisherjava)

## **`1.` DomainEvent**

Sự kiện là `Domain Events`, vì vậy, nó nên được đặt trong `domain` và được quản lý bởi `Model` chủ thể của nó

```bash
domain
 ┣ event # domain-event
 ┃ ┗ user
 ┃ ┃ ┣ AccountCreatedEvent.java
 ┃ ┃
 ┃ ┗ BaseEvent.java
 ┃
 ┣ model
   ┣ Account.java # việc khởi tạo, lưu trữ event nên được quản lý bởi model chủ thể của nó
   ┣ Email.java
   ┣ Password.java
   ┗ User.java
```

- Event: [domain.event.user.AccountCreatedEvent.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/domain/event/user/AccountCreatedEvent.java)

  ```java
  @Getter
  public class AccountCreatedEvent extends BaseEvent {
      private final String name;
      private final String email;

      private AccountCreatedEvent(UUID id, String name, String email) {
          super(id);
          this.name = name;
          this.email = email;
      }

      public static AccountCreatedEvent create(UUID id, String name, String email) {
          return new AccountCreatedEvent(id, name, email);
      }
  }
  ```

- Model quản lý event: [domain/model/Account.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/domain/model/Account.java)

  ```java
  // Các domain mode extends từ class này
  public abstract class AggregateRoot {
      // Aggregate identity
      public abstract UUID getAggregateId();
      public abstract String getAggregateType();


      // Domain Event -> phải do domain đó quản lý
      private final List<Object> events = new ArrayList<>();

      // Quản lý domain events
      protected void registerEvent(Object event) {
          events.add(event);
      }

      public List<Object> pullEvents() {
          List<Object> events = this.events;
          this.events.clear();
          return events;
      }
  }
  ```

## **`2.` Event Publisher**

Tương tự, không nên inject trực tiếp `KafkaTemplate` / `RabbitTemplate` vào `ServiceImpl`, ta cần sử dụng `EventPublisher` của tầng `/application` và viết **Adapter** hoặc **Implements** nó ở `/infrastructure`

- EventPublisher interface: [application/port/out/messaging/EventPublisher.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/application/port/out/messaging/EventPublisher.java)

  ```java
  public interface EventPublisher {
      void publishReliable(AggregateRoot aggregate, Object event);
      void publishDirect(Object event);
  }
  ```

  Khi này, ở `ServiceImpl`, ta inject **`EventPublisher`** để sử dụng: [AuthServiceImpl2.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/application/service/impl/AuthServiceImpl2.java)

- #### `@Bean` Implementations: [infrastructure/messaging/KafkaEventPublisher.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/messaging/kafka/KafkaEventPublisher.java)

  ```java

  @Component
  @RequiredArgsConstructor
  public class KafkaEventPublisher implements EventPublisher {
      private final KafkaEventRegistry kafkaEventRegistry;
      private final OutboxRepository outboxRepository;
      private final ObjectMapper objectMapper;

      private final KafkaTemplate<String, Object> kafkaTemplate; // object kafka template

      /* ========================================
      * Outbox Pattern: DÀNH CHO CÁC MESSAGE QUAN TRỌNG, BUỘC PHẢI ĐƯỢC GỬI THÀNH CÔNG
      * ĐƠN GIẢN CHỈ CẦN SAVE EVENT VÀO DB
      * NHIỆM VỤ QUÉT DB VÀ GỬI EVENT THUỘC VỀ RELAY
      ======================================== */
      @Override
      public void publishReliable(AggregateRoot aggregate, Object event) {

          KafkaEventDestination destination = kafkaEventRegistry.resolve(event);
          String payload = serialize(event); // payload

          OutboxEvent outboxEvent =
                  OutboxEvent.create(aggregate, destination, payload);
          outboxRepository.save(outboxEvent);
      }

      // -------------------------
      // Mapping Object -> Json
      private String serialize(Object event) {
          try {
              return objectMapper.writeValueAsString(event);
          } catch (Exception e) {
              throw new IllegalStateException("Failed to serialize event: " + event.getClass().getName(), e);
          }
      }

      /* ========================================
      * FIRE & FORGET
      ======================================== */
      @Override
      public void publishDirect(Object event) {
          KafkaEventDestination destination = kafkaEventRegistry.resolve(event);

          kafkaTemplate.send(destination.topic(), event)
                  .whenComplete((result, ex) -> {
                      if (ex != null) {
                          System.out.println("[Kafka] FAILED!");
                          System.out.printf("[Kafka] topic: %s, event-type: %s%n", destination.topic(), destination.typeId());
                          String topic = result.getRecordMetadata().topic();
                          int partition = result.getRecordMetadata().partition();
                          long offset = result.getRecordMetadata().offset();

                          System.out.println("[Kafka] SUCCESS!");
                          System.out.printf("[Kafka] topic: %s, event-type: %s, partition: %d, offset: %d\n",
                                  topic, destination.typeId(), partition, offset);
                      }

                      System.out.println("[Kafka] payload: " + serialize(event));
                  })
          ;
      }
  }
  ```

  **Notes**: Khi có `nhiều impl` cho `EventPulisher` (ex: Kafka, RabbitMQ, ...), **nên dựa vào config "`app.messaging.broker`" trong `application.yaml` để quyết định tạo bean nào để inject**:

  `@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "kafka")`

- #### Sau khi thêm Event vào Outbox, `Relay` sẽ thực hiện quét định kì và gửi event: [infrastructure/relay/KafkaOutboxRelay.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/relay/KafkaOutboxRelay.java)

  ```java
  @Component
  @RequiredArgsConstructor
  public class KafkaOutboxRelay {
      private final OutboxRepository outboxRepository;
      private final KafkaTemplate<String, String> kafkaTemplate;


      // logic publish lên broker được chuyển từ publisher sang relay
      @Scheduled(fixedDelayString = "${app.outbox.kafka.poll-interval-ms:5000}")
      @Transactional
      public void relay() {
          List<OutboxEvent> events =
                  outboxRepository.findPendingEventsWithLock();
          System.out.println("[KafkaOutboxRelay] PENDING events: " + events.size());
          events.forEach(this::publishAndMarkDone);
      }

      private void publishAndMarkDone(OutboxEvent event) {
          var headers = new RecordHeaders();
          headers.add("x-event-type", event.getEventType().getBytes());

          String topic = event.getTopic();
          String key = event.getAggregateId().toString();
          String payload = event.getPayload();

          kafkaTemplate.send(
                  new ProducerRecord<>(
                          topic, null, key, payload, headers)
          );

          event.markPublished();
          outboxRepository.save(event);
      }
  }
  ```

## **3. Sending Types:**

- **Simple send**:

  `kafkaTemplate.send("orders", orderJson);` // topic, message

- **Send with `MessageKey`**: message với _**cùng key**_ luôn đi vào _**cùng partition**_ => **Đảm bảo _ORDERING_**

  `kafkaTemplate.send("orders", orderId, orderJson);` // topic, key, message

  ```text
  + Khi KHÔNG CÓ key:
        dùng Round-robin để chỉ định partition =>  Sticky partitioner

  + Khi CÓ key:
        partition = hash(key) % numPartitions
  ```

  Với các message `cùng key`, nó luôn được đưa vào `CÙNG 1 PARTITION`.  
  => Đảm bảo `ORDERING` do Kafka chỉ đảm bảo order trong Partition

- **Send to `Partition`**

  `kafkaTemplate.send("orders", partition, key, value);`

- **Send with Header**

  ```java
  public void sendWithHeaders(String orderId, String orderJson, String correlationId) {
      ProducerRecord<String, String> record = new ProducerRecord<>(
              "orders",           // topic
              null,               // partition (null = để Kafka tự chọn)
              System.currentTimeMillis(), // timestamp
              orderId,            // key
              orderJson           // value
      );

      // Thêm headers — headers là metadata, không phải payload
      record.headers().add("correlationId", correlationId.getBytes(StandardCharsets.UTF_8));
      record.headers().add("source", "order-service".getBytes(StandardCharsets.UTF_8));
      record.headers().add("version", "v2".getBytes(StandardCharsets.UTF_8));

      kafkaTemplate.send(record);
  }
  ```
