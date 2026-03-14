# _**DDD**_ in **_Synchronous_** communication

## Ví dụ triển khai: [Kafka Producer](../kafka/type-headers/impl/Producer.md)

## **`1.` Event**

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
  // Model
  @Getter
  public class Account {

      // ____________________ Quản lý event của nó ____________________ //
      private final List<Object> events = new ArrayList<>();
      // ____________________ Create event ____________________ //
      private void addEvent(Object event) {
          this.events.add(event);
      }
      // ____________________ Publish event ____________________ //
      public List<Object> pullEvents() {
          val events = List.copyOf(this.events);
          // clear event
          this.events.clear();

          return events;
      }

      // other class members
  }
  ```

## **`2.` Event Publisher**

Tương tự, không nên inject trực tiếp `KafkaTemplate` / `RabbitTemplate` vào `ServiceImpl`, ta cần sử dụng `EventPublisher` của tầng `/application` và viết **Adapter** hoặc **Implements** nó ở `/infrastructure`

- Event Publisher: [application/port/out/messaging/EventPublisher.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/application/port/out/messaging/EventPublisher.java)

  ```java
  public interface EventPublisher {
      void publish(Object event);
  }
  ```

- Implementations:
  - [infrastructure/messaging/KafkaEventPublisher.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/messaging/KafkaEventPublisher.java)

    ```java
    @Component
    @RequiredArgsConstructor
    public class KafkaEventPublisher implements EventPublisher {
        // KafkaTemplate
        private final KafkaTemplate<String, Object> kafkaTemplate;

        @Override
        public void publish(Object event) {

            Map<String, String> eventProps = resolveTopicAndKey(event);
            String topic = eventProps.get("topic");
            String key = eventProps.get("key");

            CompletableFuture<SendResult<String, Object>> success = kafkaTemplate.send(topic, key, event);

            // xử lý kết quả
            success.whenComplete((result, exception) -> {
                if (exception != null) {
                    System.out.printf("[Async Communicate - Kafka]: FAILED send message with id: %s\n", key);
                } else {
                    System.out.printf("[Async Communicate - Kafka]: send message with id: %s\n", key);
                    System.out.printf(
                        "[Async Communicate - Kafka]: topic: %s, partition: %d, offset: %d\n",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                    );
                }
            });
        }

        private Map<String, String> resolveTopicAndKey(Object event) {
            Map<String, String> props = new HashMap<>();
            switch (event) {

                case AccountCreatedEvent e -> {
                    props.put("topic", KafkaProduceConstants.UserEvents.USER_EVENTS_TOPIC);
                    props.put("key", e.getId().toString());
                }
                // case...

                // nếu event chưa được định nghĩa
                default -> throw new IllegalArgumentException("Unknown event: " + event.getClass());
            }

            return props;
        }
    }
    ```

  - [infrastructure/messaging/RabbitMQPublisher.java](/codes/monorepo-microservice-example/user-service/src/main/java/vduczz/userservice/infrastructure/messaging/RabbitMQPublisher.java)

    ```java
    @Component
    @RequiredArgsConstructor
    public class RabbitMQPublisher implements EventPublisher {
        private final RabbitTemplate rabbitTemplate;

        @Override
        public void publish(Object event) {

            Map<String, String> props = resolveExchangeAndKey(event);

            rabbitTemplate.convertAndSend(
                props.get("exchange"), // exchange
                props.get("key"), // key
                // exchange + key -> topic
                event, // message
                // correlation
                new CorrelationData(props.get("correlationId"))
            );
        }

        private Map<String, String> resolveExchangeAndKey(Object event) {
            Map<String, String> props = new HashMap<>();

            switch (event) {
                case AccountCreatedEvent e -> {
                    props.put(
                            "exchange",
                            RabbitMQConfig.EXCHANGE);
                    props.put(
                            "key",
                            RabbitMQConfig.ROUTING_KEY
                    );
                    props.put("correlationId", e.getId().toString());
                }

                default -> throw new IllegalArgumentException("Unknown event: " + event.getClass());
            }

            return props;
        }
    }
    ```

  - **Notes**: Khi có `nhiều impl` cho `EventPulisher` (ex: Kafka, RabbitMQ, ...), **nên dựa vào config "`app.messaging.broker`" trong `application.yaml` để quyết định tạo bean nào để inject**

    `@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "kafka")`
