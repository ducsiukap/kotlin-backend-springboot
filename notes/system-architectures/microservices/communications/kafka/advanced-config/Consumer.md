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

## `TypeMapping` consumer side (for Json-Deserializer):

```kotlin
@Configuration
class KafkaConsumerConfig(
    private val kafkaProperties: KafkaProperties
) {
    // ____________________ Customer TypeMapper (JSON - Deserializer) ____________________ //
    @Bean
    fun idMappings(): DefaultJacksonJavaTypeMapper {
        // Bean tạo TypeMapper

        val typeMapper = DefaultJacksonJavaTypeMapper()
        typeMapper.typePrecedence = JacksonJavaTypeMapper.TypePrecedence.TYPE_ID

        val mappings = HashMap<String, Class<*>>()
        mappings[KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V1] = WelcomeMailRequest::class.java

        typeMapper.idClassMapping = mappings
        return typeMapper
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        // Bean custom ConsumerFactory
        // gắn TypeMapper vào ConsumerFactory

        // auto-config Kafka properties for consumer
        val configProps = kafkaProperties.buildConsumerProperties()

        // init deserializer
        val deserializer = JacksonJsonDeserializer<Any>();
        deserializer.typeMapper = idMappings() // spring.json.type.mapping
        deserializer.trustedPackages("*") // spring.json.trusted.packages
        deserializer.setUseTypeHeaders(true) // spring.json.use.type.headers
        // cần xóa auto-config để tránh conflict
        configProps.remove("spring.json.use.type.headers");
        configProps.remove("spring.json.type.mapping");
        configProps.remove("spring.json.trusted.packages");

        return DefaultKafkaConsumerFactory<String, Any>(
            configProps,
            StringDeserializer(),
            deserializer,
        )
    }


    // tùy chỉnh config ListenerContainer
    @Bean
    fun kafkaListenerContainerFactory(
        // những thứ đã config trong yaml
        // vì đã custom nên nó sẽ inject consumerFactory bên trên
        consumerFactory: ConsumerFactory<String, Any>
    ): ConcurrentKafkaListenerContainerFactory<String, Any> {

        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()

        // gắn consumer factory
        factory.setConsumerFactory(consumerFactory)

        // thêm các cấu hình nâng cao
        // hoặc tùy chỉnh các cấu hình
        factory.setConcurrency(3) // thread consumer concurrency

        return factory
    }
}

```
