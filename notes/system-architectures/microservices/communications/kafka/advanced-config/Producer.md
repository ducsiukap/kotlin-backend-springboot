# Some _**Advanced Configuration**_ for `Producer`

### `Create new topic`

```java
// package /infrastructure/config/messagequeue/kafka/KafkaProducerConfig;

@Configuration
class KafkaProducerConfig {

    // ____________________ create ONE topic ____________________ //
    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder
                .name(KafkaProduceConstants.UserEvents.USER_EVENTS_TOPIC)
                .partitions(3) // 3 partition -> parallel processing
                .replicas(1) // 1 replica
                .build();
    }

    // ____________________ create MULTIPLE topics ____________________ //
    @Bean
    public KafkaAdmin.NewTopics allTopics() {
        return new KafkaAdmin.NewTopics(
            // Topic #1
            TopicBuilder.name("orders")
                .partitions(3).replicas(1)
                .build(),
            // Topic #2
            TopicBuilder.name("payments")
                .partitions(3).replicas(1)
                .build(),
            // Topic #3
            TopicBuilder.name("notifications")
                .partitions(1).replicas(1)
                .build()
        );
    }
}
```

### `TypeMapping` producer side

```java
@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    // ____________________ TypeMapping ____________________ //
    @Bean
    public DefaultJacksonJavaTypeMapper typeMapper() {
        // Bean to create TypeMapper

        // init TypeMapper
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        // => ưu tiên sử dụng type header // __TypeId__
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.TYPE_ID);

        // mappings
        Map<String, Class<?>> mappings = new HashMap<>();
        // put(alias, FQN)
        mappings.put(
            KafkaProduceConstants.UserEvents.EventTypes.USER_CREATED_V1,
            WelcomeMailRequest.class
        );

        // __TypeId__ -> Class
        typeMapper.setIdClassMapping(mappings);

        return typeMapper;
    }

    // Sử dụng TypeMapper trong ProducerFactory
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        // Bean to custom config producer - ProducerFactory

        // sử dụng các config có sẵn trong application.yaml
        Map<String, Object> configProps = kafkaProperties.buildProducerProperties(); // chỉ sử dụng các thuộc tính cho producer

        // init serializer
        JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>();
        serializer.setTypeMapper(typeMapper()); // addTypeMapper -> spring.json.type.mapper
        serializer.setAddTypeInfo(true); // addTypeInfo -> spring.json.add.type.headers
        // cần hủy auto-config tương ứng trong application.yml
        configProps.remove("spring.json.add.type.headers");
        configProps.remove("spring.json.type.mapping");


        return new DefaultKafkaProducerFactory<>(
            configProps, // config có sẵn trong yaml
            new StringSerializer(), // key serializer
            serializer // value serializer
        );
    }

    @Bean
    // sử dụng custom ProducerFactory cho KafkaTemplate
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```
