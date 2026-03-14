package vduczz.userservice.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vduczz.userservice.domain.model.AggregateRoot;
import vduczz.userservice.infrastructure.messaging.kafka.KafkaEventDestination;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(name = "idx_kafka_aggregate", columnList = "aggregate_type, aggregate_id"),
                @Index(name = "idx_kafka_event_status", columnList = "status, published_at, occurred_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {
    @Id
    @Column(columnDefinition = "CHAR(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "aggregate_id", columnDefinition = "CHAR(36)", nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID aggregateId;
    @Column(nullable = false, name = "aggregate_type")
    private String aggregateType;

    @Column(nullable = false)
    private String topic;

    @Column(name = "event_type", nullable = false)
    private String eventType; // consumer dựa vào event-type để resolve

    @Column(nullable = false, columnDefinition = "JSON")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    public static OutboxEvent create(
            AggregateRoot aggregateRoot,
            KafkaEventDestination destination,
            String payload
    ) {
        OutboxEvent entity = new OutboxEvent();
        // Outbox entity information
        entity.id = UUID.randomUUID();
        entity.occurredAt = Instant.now();
        entity.publishedAt = null;
        entity.status = EventStatus.PENDING;

        // AggregateRoot information
        entity.aggregateId = aggregateRoot.getAggregateId();
        entity.aggregateType = aggregateRoot.getAggregateType();

        // Kafka Topic information
        entity.topic = destination.topic();

        // Message information
        entity.eventType = destination.typeId();
        entity.payload = payload;

        return entity;
    }

    public boolean isPending() {
        return this.status == EventStatus.PENDING;
    }

    public void markPublished() {
        this.status = EventStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public void markFailed() {
        this.status = EventStatus.FAILED;
    }
}
