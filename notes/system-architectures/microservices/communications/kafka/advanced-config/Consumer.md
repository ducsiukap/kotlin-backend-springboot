# Some _**Advaned Configurations**_ cho Consumer

Sử dụng khi cần tùy chỉnh hành vi của **ListenerContainer** (cấu hình nâng cao)

- `ErrorHandler`
- `RecordMessageConverter` (for StringDeserializer, ...)
- `TypeMapper`
- `TransactionManager`
- `RecordInterceptor`
- `BatchListener`
- ...

```java

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        // inject ConsumerFactory: auto-configuration từ application.yaml
        ConsumerFactory<String, String> consumerFactory,
        KafkaTemplate<String, String> kafkaTemplate
    ) {

        // factory dùng để config
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

        // set consumer factory
        factory.setConsumerFactory(consumerFactory);

        // Số luồng đồng thời — mỗi luồng xử lý 1 partition
        // Nên đặt = số partition của topic để đạt parallelism tối đa
        // không được lớn hơn số partitiona của topic
        factory.setConcurrency(3);

        // other configurations

        return factory;
    }
}
```

**Sử dụng `ConcurrentKafkaListenerContainerFactory` thay cho `KafkaMessageListenerContainer `**:

> _Thực tế `KafkaListenerContainerFactory` là `interface` gốc, tạo ra container để chạy listener. Spring Kafka cung cấp 2 implements cho interface đó:_

- `KafkaMessageListenerContainer` chạy **Single-Thread**: `1 thread` xử lý **tất cả các `partition`** được assign cho consumer đó.
- `ConcurrentKafkaListenerContainerFactory` về bản chất, nó bọc nhiều `KafkaMessageListenerContainer` bên trong thông qua việc thiết lập số lượng `concurrency` _(ex: concurrency=3)_

  Khi thiết lập `concurrency=1`, `ConcurrentKafkaListenerContainerFactory` cũng bao hàm khả năng của `KafkaMessageListenerContainer`.

Vì vậy **trong thực tế**, người ta gần như luôn dùng `ConcurrentKafkaListenerContainerFactory` vì nó linh hoạt hơn mà không có nhược điểm gì.

---

## `RecordMessageConverter`: for **StringDeserializer**

> _Được sử dụng để config tự động chuyển **`String -> JSON`**, nhờ vậy có thể **dễ dàng mapping sang object** bằng cách thay đổi **kiểu dữ liệu** của **tham số `@Payload`** trong consume của `KafkaListener` từ **`String`** -> **`Specific-Class`** mong muốn._

```java
// ____________________ RecordMessageConverter ____________________ //
factory.setRecordMessageConverter(
    // sử dụng có sẵn của spring
    StringJacksonJsonMessageConverter()
)
```

**Exception**: nếu `JSON string` **không hợp lệ** hoặc **không match** với `OrderDto` (kiểu được chỉ định làm `@Payload` cho hàm consume), **Spring Kafka sẽ ném `MessageConversionException`**  
**Note**: Vì lỗi này không bao giờ tự hết nếu retry, nên đánh dấu nó là non-retryable trong `ErrorHandler`

---

## `BatchListener`: Cấu hình xử lý theo batch

> _Hàm consume của `KafkaListener` có thể nhận tham số `List<RecordMessage<KeyType, ValueType>> batchRecords` để xử lý theo batch_

- Cần factory riêng cho trường hợp xử lý theo batch., ex: `batchKafkaListenerContainerFactory`
- `@KafkaListener` cần chỉ định tham số `containerFactory`
  ```java
  @KafkaListener(
    containerFactory = "batchKafkaListenerContainerFactory",
    ...
  )
  ```

```java
// Factory riêng cho batch listener
@Bean
public ConcurrentKafkaListenerContainerFactory<String, String> batchKafkaListenerContainerFactory(
    ConsumerFactory<String, String> consumerFactory
) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);

    factory.setBatchListener(true);  // Bật batch mode

    // ack-mode
    factory.getContainerProperties().setAckModeA(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

    return factory;
}
```

---