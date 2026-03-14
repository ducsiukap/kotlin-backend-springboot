package vduczz.userservice.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import vduczz.userservice.application.port.out.gateway.dto.request.WelcomeMailRequest;
import vduczz.userservice.application.port.out.messaging.EventPublisher;
import vduczz.userservice.domain.event.user.AccountCreatedEvent;
import vduczz.userservice.domain.model.AggregateRoot;
import vduczz.userservice.infrastructure.config.messagequeue.rabbitmq.RabbitMQConstant;
import vduczz.userservice.infrastructure.config.messagequeue.rabbitmq.RabbitNotificationConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
// @ConditionalOnProperty(name = "app.messsaging.broker", havingValue = "rabbitmq")
public class RabbitMQPublisher implements EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishReliable(AggregateRoot aggregate, Object event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void publishDirect(Object event) {

        Map<String, String> props = resolveExchangeAndKey(event);


        rabbitTemplate.convertAndSend(
                props.get("exchange"), // exchange
                props.get("key"), // key
                // exchange + key -> topic

                event, // message

                // correlation
                new CorrelationData(
                        props.get("correlationId")
                )
        );
    }

    private Map<String, String> resolveExchangeAndKey(Object event) {
        Map<String, String> props = new HashMap<>();

        switch (event) {
            case AccountCreatedEvent e -> {
                props.put("exchange", RabbitMQConstant.Exchanges.EXCHANGE);
                props.put("key", RabbitMQConstant.RoutingKeys.ROUTING_KEY_V2);
                props.put("correlationId", e.getId().toString());
            }

            case WelcomeMailRequest e -> {
                props.put("exchange", RabbitMQConstant.Exchanges.EXCHANGE);
                props.put("key", RabbitMQConstant.RoutingKeys.ROUTING_KEY_V1);
                props.put("correlationId", UUID.randomUUID().toString());
            }

            default -> throw new IllegalArgumentException("Unknown event: " + event.getClass());
        }

        return props;
    }
}
