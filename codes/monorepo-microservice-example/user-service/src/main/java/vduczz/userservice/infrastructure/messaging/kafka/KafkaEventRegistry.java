package vduczz.userservice.infrastructure.messaging.kafka;

import org.springframework.stereotype.Component;
import vduczz.userservice.application.port.out.gateway.dto.request.WelcomeMailRequest;
import vduczz.userservice.domain.event.user.AccountCreatedEvent;
import vduczz.userservice.infrastructure.config.messagequeue.kafka.KafkaProduceConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaEventRegistry {

    // mapping Topic <-> Event
    private static final Map<Class<?>, KafkaEventDestination> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(
                WelcomeMailRequest.class,
                new KafkaEventDestination(
                        KafkaProduceConstants.UserEvents.USER_EVENTS_TOPIC,
                        KafkaProduceConstants.UserEvents.EventTypes.USER_CREATED_V1
                )
        );
        REGISTRY.put(AccountCreatedEvent.class,
                new KafkaEventDestination(
                        KafkaProduceConstants.UserEvents.USER_EVENTS_TOPIC, // topic
                        KafkaProduceConstants.UserEvents.EventTypes.USER_CREATED_V2 // typeId
                ));
    }

    private KafkaEventRegistry() {
    }

    public KafkaEventDestination resolve(Object event) {

        Class<?> eventClass = event.getClass();
        KafkaEventDestination destination = REGISTRY.get(eventClass);

        if (destination == null) {
            throw new IllegalArgumentException(
                    "No destination registered for type: "
                            + eventClass.getSimpleName());
        }

        return destination;
    }

    public String resolveTopic(Object event) {
        return resolve(event).topic();
    }
}
