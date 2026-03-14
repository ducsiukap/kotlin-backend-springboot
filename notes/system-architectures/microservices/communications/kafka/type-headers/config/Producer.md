# **Producer _Type mappings_ Config**

### `application.yml`:

```yml
spring:
  kafka:
    bootstrap-server: localhost:9094
    producer:
      # serialize
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JacksonJsonSerializer # Json-serialize
      # value-serializer: org.apache.kafka.common.serialization.StringSerializer # String-serialize

      properties:
        spring.json.add.type.headers: true # tự động thêm TypeHeader khi serialize object
        enable.idempotence: true
```

### `TypeMapping` cho **_JsonSerializer_**

```java
@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    // kafka auto-config in application.yaml
    private final KafkaProperties kafkaProperties;

    /* ========================================
    * TYPE-MAPPER for JacksonJsonSerializer
    ======================================== */
    // -------------------------
    // TYPE-MAPPER
    @Bean
    public DefaultJacksonJavaTypeMapper typeMapper() {
        // Bean to create TypeMapper

        DefaultJacksonJavaTypeMapper typeMapper =
                new DefaultJacksonJavaTypeMapper(); // init TypeMapper
        typeMapper.setTypePrecedence( // => ưu tiên sử dụng type header
                JacksonJavaTypeMapper.TypePrecedence.TYPE_ID // __TypeId__
        );

        /* ========================================
        * ĐỔI TÊN CỦA TYPE HEADER TRONG HEADRE
        * __TypeId__ -> x-event-type
        ======================================== */
        typeMapper.setClassIdFieldName("x-event-type");

        Map<String, Class<?>> mappings = new HashMap<>(); // mappings alias <-> type
        // put(alias, FQN)
        mappings.put(
                KafkaProduceConstants.UserEvents.EventTypes.USER_CREATED_V1, // alias
                WelcomeMailRequest.class // Type (FQN)
        );
        mappings.put(
                KafkaProduceConstants.UserEvents.EventTypes.USER_CREATED_V2,
                AccountCreatedEvent.class
        );

        typeMapper.setIdClassMapping(mappings); // __TypeId__ -> Class
        return typeMapper;
    }

    // -------------------------
    // BEAN TO CUSTOM ProducerFactory (CUSTOM PRODUCER CONFIG)
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        // USE TYPE-MAPPER IN ProducerFactory (PRODUCER CONFIG)

        Map<String, Object> configProps
                = kafkaProperties.buildProducerProperties(); // auto-config cho producer

        JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>(); // init serializer
        serializer.setTypeMapper(typeMapper()); // addTypeMapper -> spring.json.type.mapper
        serializer.setAddTypeInfo(true); // addTypeInfo -> spring.json.add.type.headers
        // nguyên tắc: 1 thuộc tính chỉ có thể được khởi tạo giá trị thông qua:
        //      + serializer.set...()
        //      + hoặc, config trong application.yaml
        // tức là chỉ setABC() hoặc config abc trong yaml, không được cả 2
        configProps.remove("spring.json.add.type.headers");
        configProps.remove("spring.json.type.mapping");

        return new DefaultKafkaProducerFactory<>(
                configProps, // config có sẵn trong yaml
                new StringSerializer(), // key serializer
                serializer // value serializer
        );
    }

    // -------------------------
    // BEAN TO DEFINE KafkaTemplate BASED ON CUSTOM PRODUCER CONFIG (above)
    @Bean("objectKafkaTemplate")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### Có thể thêm Bean `KafkaTemplate<String, String>` cho _**StringSerialize**_

```java

    /* ========================================
    * CREATE KafkaTemplate<String, String>  => KHÔNG JsonSerialize, KHÔNG TypeHeader
    ======================================== */
    @Bean("stringKafkaTemplate")
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        var producerProps = kafkaProperties.buildProducerProperties();
        var producerFactory = new DefaultKafkaProducerFactory<>(
                producerProps,
                new StringSerializer(),
                new StringSerializer() // value-serializer
        );
        return new KafkaTemplate<>(producerFactory);
    }
```
