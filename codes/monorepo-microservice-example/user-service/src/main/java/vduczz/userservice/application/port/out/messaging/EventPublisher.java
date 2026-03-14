package vduczz.userservice.application.port.out.messaging;

import vduczz.userservice.domain.model.AggregateRoot;

public interface EventPublisher {
    void publishReliable(AggregateRoot aggregate, Object event);
    void publishDirect(Object event);
}
