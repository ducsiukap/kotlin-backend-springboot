package vduczz.userservice.infrastructure.config.messagequeue;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


    // ============================================================
    // Main Queue
    public static final String QUEUE = "notification.welcome.queue";
    public static final String EXCHANGE = "user.events.exchange";
    // ============================================================
    // DLQ - Dead Letter Queue
    public static final String DLQ_NOTIFICATION = "notification.welcome.dlq";
    public static final String DLX_NAME = "user.events.dlx";
    // ============================================================
    // Common routing key
    public static final String ROUTING_KEY = "user.created";

    // ============================================================
    // DLQ Binding
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NOTIFICATION).build();
        //= new Queue(DLQ_NOTIFICATION, true);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX_NAME);
        // = ExchangeBuilder.topicExchange(DLX_NAME).build();
    }

    // Binding
    @Bean
    public Binding dlqBinding(
            Queue deadLetterQueue,
            TopicExchange deadLetterExchange
    ) {
        return BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(ROUTING_KEY);
    }

    // ============================================================
    // MainQueue Binding
    @Bean
    public Queue notificationWelcomeQueue(
            TopicExchange deadLetterExchange // inject Bean đảm bảo tạo dlq trước
    ) {
        // return new Queue(QUEUE, true);
        // durable=true: RabbitMQ sập thì khi khởi động lại, Queue vẫn còn.

        return QueueBuilder.durable(QUEUE)
                // DLQ config => bổ sung dlq cho queue chính
                // nếu có message lỗi thì đưa vào DLQ qua DLX và KEY được cung cấp
                .withArgument("x-dead-letter-exchange", deadLetterExchange.getName()) // DLX
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY) // KEY
                // .withArgument("x-message-ttl", 60000) // 60 giây TTL
                .build();
    }

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Binding
    @Bean
    public Binding notificationWelcomeBinding(
            Queue notificationWelcomeQueue,
            TopicExchange userEventsExchange
    ) {
        return BindingBuilder
                .bind(notificationWelcomeQueue)
                .to(userEventsExchange)
                .with(ROUTING_KEY);
    }

    // ============================================================
    // Message Converter
    // JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // ============================================================
    // RabbitTemplate config
    //  + enable callback
    //  + specific message converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        // set converter
        template.setMessageConverter(jsonMessageConverter());

        // bật confirm callback
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) System.err.println("Message NACK: " + cause);
        });

        // callback (handle returned message)
        template.setReturnsCallback(returned ->
                System.err.println("Message returned: " + returned.getMessage())
        );
        return template;
    }
}
