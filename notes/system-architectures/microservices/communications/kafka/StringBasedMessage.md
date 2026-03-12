# **_`String-based`_** messaging

### **`Use cases`**

- `plain-text` / `JSON string` / `CSV` / ... payload.
- Fast Prototype.

### **_Bad_ `Usecases`**:

- **Large** payload and need to **optimize netword**
- **Chema _validation_**
- **Type-safety**.

## **`1.` Properties _Configuration_**

```yml
# application.yaml

spring:
  kafka:
    # commons
    bootstrap-servers: localhost:9094

    # // ____________________ PRODUCER side ____________________ //
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer

      # phía producer có thể linh hoạt gửi payload theo Json/String format
      value-serializer: org.apache.kafka.common.serialization.StringSerializer # String-serializer
      # value-serializer: org.springframework.kafka.support.serializer.JacksonJsonSerializer;
      # JsonSerializer (phiên bản mới dùng JacksonJsonSerializer)

    # // ____________________ CONSUMER side ____________________ //
    consumer:
      group-id: my-default-group

      auto-offset-reset: earliest # Đọc từ đầu nếu chưa có offset
      # earliest: đọc từ đầu nếu chưa có offset
      # latest (default): đọc message mới từ khi start

      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer # String-deserializer
    listener:
      ack-mode: record
      # RECORD:
      #         Commit offset ngay sau khi Listener xử lý xong từng bản ghi.
      # BATCH: (default)
      #         Commit offset sau khi xử lý xong tất cả bản ghi nhận được từ một lần poll()
      # TIME:
      #         Commit offset sau khi xử lý xong bản ghi, miễn là đã quá khoảng thời gian ackTime
      # COUNT:
      #         Commit offset khi số lượng bản ghi đã xử lý đạt mức ackCount.
      # MANUAL:
      #         Listener chịu trách nhiệm gọi Acknowledgment.acknowledge(), nhưng Spring sẽ gom lại để commit sau.
      # MANUAL_IMMEDIATE:
      #         Gọi acknowledge() phát là Spring gửi lệnh commit lên Kafka ngay lập tức (blocking)
      concurrency: 3 # concurrent consume thread
      poll-timeout: 3000
```

## **`2.` _`Producer`_ side**

#### _Sending_ (`produce`) Message\*\*

```java
// package:
// application.service.impl.UserServiceImpl.java

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    // dependencies...

    // inject KafkaTemplate to sending messsage
    // + StringSerializer -> KafkaTemplate<String, String>
    //      => CompletableFuture<String, String>
    // + JacksonJsonSerializer -> KafkaTemplate<String, Object>
    //      => CompletableFuture<String, Object>
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void register(
        // dto...
        RequestDto request
    ) {

        // processing
        // YourDomainModel model = ...

        // >>>>>>>>>>>>>>>>>>>>>>>>> Asynchronous Send message <<<<<<<<<<<<<<<<<<<<<<<<< //
        CompletableFuture<String, Object> sendResult = kafkaTemplate.send("orders", model.id, model)
        // StringBased -> có thể gửi String, JSON-object, ...

        // ____________________ Process result ____________________ //
        sendResult.whenComplete(
            (result, exception) -> {
                if (exception == null) { // send successfully

                    RecordMetadata metadata = result.getRecordMetadata(); // topic, partition, offset
                    // log...

                } else { // send failed

                    // log...

                }
            }
        )


        // >>>>>>>>>>>>>>>>>>>>>>>>> synchronous sending <<<<<<<<<<<<<<<<<<<<<<<<< //
        // SendResult<String, Object> result = kafkaTemplate.send(...).get()
    }

}
```

**Sending Types**

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

---

## **`3.` _Consumer_ side**

### **`3.1.` Custom Consumer config**

Có thể config để tự động convert `String -> Json` để có thể parse thành bất kì type nào mà listener sử dụng:

```java
@Configuration
class KafkaConsumerConfig {

    @Bean
    fun kafkaListenerContainerFactory(

        // những thứ đã config trong application.yaml
        consumerFactory: ConsumerFactory<String, String>

    ): ConcurrentKafkaListenerContainerFactory<String, String> // String key ,String Value
    // ConcurrentKafkaListenerContainerFactory -> config ListenerContainer
    {

        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()

        // Sử dụng AutoConfig từ những props có trong application.yaml về consumer và listener
        factory.setConsumerFactory(consumerFactory)

        // thêm các cấu hình nâng cao
        // hoặc tùy chỉnh các cấu hình
        factory.setConcurrency(3); // thread consumer concurrency
        // convert string -> JSON
        factory.setRecordMessageConverter(
            // sử dụng có sẵn của spring
            StringJacksonJsonMessageConverter()
        )

        return factory
    }
}
```

Exception: nếu `JSON string` **không hợp lệ** hoặc **không match** với `OrderDto` (example object class in cumsume function), **Spring Kafka sẽ ném `MessageConversionException`**  
**Note**: Vì lỗi này không bao giờ tự hết nếu retry, bạn nên đánh dấu nó là non-retryable trong error handler (**Xem config nâng cao**!)

### **`3.2.` Listen event**

```java
@Component
@Slf4j // log
public class OrderConsumer {

    @KafkaListener(
        topics = "orders",
        groupId = "order-processor"
    )
    public void consume(

        // Nếu không có config String -> JSON bên trên
        // -> Nhận @Payload dạng String
        String event

        // Nếu có config String -> JSON
        // -> Nhận trực tiếp theo class mong muốn
        // YourDto event


        // @Header / @Headers
        // Acknowledge ack
        // ConsumerRecord<String, ...>
        // List<ConsumerRecord<String, ...>> // for ack-mode: batch

    ) {
        log.info("Received order: {}", event.orderId());
        // process event
    }
}
```

#### **3.2.1. `@KafkaListener`**

- Listen theo 1 topic:
  ```java
  @KafkaListener(
    topics = "orders",
    groupId = "order-processor"
  )
  ```
- Listen nhiều topic cùng lúc:
  ```java
  @KafkaListener(t
      topics = {"orders.created", "orders.updated", "orders.cancelled"}
      groupId = "order-processor"
  )
  ```
- Listen từ topic theo pattern:
  ```java
  @KafkaListener(
      topicPattern = "orders\\..*",
      groupId = "order-processer"
  )
  ```
