package vduczz.userservice.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import vduczz.userservice.application.port.out.messaging.EventPublisher;
import vduczz.userservice.domain.model.AggregateRoot;
import vduczz.userservice.infrastructure.outbox.OutboxEvent;
import vduczz.userservice.infrastructure.outbox.OutboxRepository;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
