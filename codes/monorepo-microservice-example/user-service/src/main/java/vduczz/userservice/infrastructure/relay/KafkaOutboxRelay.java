package vduczz.userservice.infrastructure.relay;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vduczz.userservice.infrastructure.outbox.OutboxEvent;
import vduczz.userservice.infrastructure.outbox.OutboxRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaOutboxRelay {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;


    // logic publish lên broker được chuyển từ publisher sang relay
    @Scheduled(fixedDelayString = "${app.outbox.kafka.poll-interval-ms:5000}")
    @Transactional
    public void relay() {
        List<OutboxEvent> events =
                outboxRepository.findPendingEventsWithLock();
        System.out.println("[KafkaOutboxRelay] PENDING events: " + events.size());
        events.forEach(this::publishAndMarkDone);
    }

    private void publishAndMarkDone(OutboxEvent event) {
        var headers = new RecordHeaders();
        headers.add("x-event-type", event.getEventType().getBytes());

        String topic = event.getTopic();
        String key = event.getAggregateId().toString();
        String payload = event.getPayload();

        kafkaTemplate.send(
                new ProducerRecord<>(
                        topic, null, key, payload, headers)
        );

        event.markPublished();
        outboxRepository.save(event);
    }
}
