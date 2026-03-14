package vduczz.userservice.domain.event.user;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BaseEvent {

    // common event field
    private final UUID id;
    private final Instant occurredOn; // UTC time

    protected BaseEvent(UUID id) {
        this.id = id;
        this.occurredOn = Instant.now();
    }
}
