package vduczz.userservice.infrastructure.messaging.kafka;

public record KafkaEventDestination(
        String topic,
        String typeId
) {}
