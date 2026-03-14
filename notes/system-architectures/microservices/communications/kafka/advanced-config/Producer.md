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
