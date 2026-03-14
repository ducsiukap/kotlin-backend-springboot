# **Consumer _Type mappings_ config**

## `application.yml`

```yml
spring
  kafka:
    bootstrap-servers: localhost:9094
    consumer:
      auto-offset-reset: latest
      # earliest: đọc từ đầu nếu chưa có offset
      # latest (default): đọc message mới từ khi start

      # group-id
      group-id: notification-service-group

      # deserialize
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JacksonJsonDeserializer # Json - Deserialize
      # value-deserializer: org.apache.kafka.common.serialization.StringDeserializer # String - Deserialize // đọc thành string xong chuyển json sau

      properties:
        # trust all package
        # => cho phép Jackson deserialize từ bất kì package nào
        spring.json.trusted.packages: "*"
        spring.json.type.header.name: "x-event-type" # thuộc tính của header để lấy làm TypeMapping
```

## `TypeMapping` consumer side (for Json-Deserializer):

```kotlin
@Configuration
class KafkaConsumerConfig(
    private val kafkaProperties: KafkaProperties
) {

    /* ========================================
    * TYPE-MAPPING KAFKA LISTENERS // JSON DESERIALIZE
    * MAPPINGS VIA __TypeId__ (or x-event-type)
    ======================================== */
    // -------------------------
    // BEAN TO CREATE MAPPING TypeHeader <-> Class
    @Bean
    fun typeMapper(): DefaultJacksonJavaTypeMapper {
        val typeMapper = DefaultJacksonJavaTypeMapper()
        typeMapper.typePrecedence = JacksonJavaTypeMapper.TypePrecedence.TYPE_ID

        val mappings = HashMap<String, Class<*>>()
        mappings[KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V1] = WelcomeMailRequest::class.java
        mappings[KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V2] = WelcomeMailRequest2::class.java

        typeMapper.idClassMapping = mappings
        typeMapper.addTrustedPackages("*")
        return typeMapper
    }

    // -------------------------
    // BEAN APPLY TYPE-MAPPER TO ConsumerFactory
    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        // auto-config Kafka properties for consumer
        val configProps = kafkaProperties.buildConsumerProperties()

        val deserializer = JacksonJsonDeserializer<Any>();
        deserializer.typeMapper = typeMapper() // spring.json.type.mapping
        // trong TypeMapper đã config setTrustedPackages sẵn // spring.json.trusted.packages
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

    // -------------------------
    // BEAN CUSTOM CONFIG LISTENER -> APPLY custom ConsumerFactory
    @Bean
    @Primary
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

## **Bean** khởi tạo `KafkaListenerContainerFactory` cho **StringDeserialize**

```java
    /* ========================================
    * Bean create value-deserializer = StringDeserialize
    * For ConsumerFactory
    ======================================== */
    @Bean("stringKafkaListenerContainerFactory")
    fun stringKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {

        // ConsumerFactory
        val consumerProps = kafkaProperties.buildConsumerProperties()
        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(
            consumerProps,
            StringDeserializer(),
            StringDeserializer(),
        )

        // KafkaListenerContainerFactory
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.setConsumerFactory(consumerFactory)

        // có thể config auto convert Json -> String tại đây
        // nhưng nếu vậy thì bỏ qua event_type => không nên
        // factory.setRecordMessageConverter(StringJacksonJsonMessageConverter());

        return factory
    }
```
