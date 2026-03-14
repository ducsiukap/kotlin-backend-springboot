package vduczz.userservice.infrastructure.config.messagequeue.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitNotificationConfig {

    // Consumer nên là người tạo Queue và Binding
    // Producer chỉ cần cung cấp key và exchange
    // Khuyên khích vả 2 đều thực hiện tạo và binding
    // ============================================================
    // Main Queue
    public static final String NOTIFICATION_QUEUE_V1 = "notification.welcome.queue.v1";
    public static final String NOTIFICATION_QUEUE_V2 = "notification.welcome.queue.v2";
    // DLQ - Dead Letter Queue
    public static final String NOTIFICATION_DLQ_V1 = "notification.welcome.dlq.v1";
    public static final String NOTIFICATION_DLQ_V2 = "notification.welcome.dlq.v2";
    // Common routing key


    /* ========================================
    * Declarables: xử lý khi quá nhiều queue cần map
    ======================================== */
    @Bean
    public Declarables userQueueBindings() {
        var notiDLQ1 = new Queue(NOTIFICATION_DLQ_V1, true);
        var notiDLQ2 = new Queue(NOTIFICATION_DLQ_V2, true);
        var dlx = new TopicExchange(RabbitMQConstant.Exchanges.DLX);
        var notiDLQ1Binding = BindingBuilder.bind(notiDLQ1).to(dlx).with(RabbitMQConstant.RoutingKeys.ROUTING_KEY_V1);
        var notiDLQ2Binding = BindingBuilder.bind(notiDLQ2).to(dlx).with(RabbitMQConstant.RoutingKeys.ROUTING_KEY_V2);

        var notiQueue1 = QueueBuilder.durable(NOTIFICATION_QUEUE_V1)
                .withArgument("x-dead-letter-exchange", RabbitMQConstant.Exchanges.DLX)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstant.RoutingKeys.ROUTING_KEY_V1)
                .build();
        var notiQueue2 = QueueBuilder.durable(NOTIFICATION_QUEUE_V2)
                .withArgument("x-dead-letter-exchange", RabbitMQConstant.Exchanges.DLX)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstant.RoutingKeys.ROUTING_KEY_V2)
                .build();
        var exchange = new TopicExchange(RabbitMQConstant.Exchanges.EXCHANGE);
        var notiQueue1Binding = BindingBuilder.bind(notiQueue1).to(exchange).with(RabbitMQConstant.RoutingKeys.ROUTING_KEY_V1);
        var notiQueue2Binding = BindingBuilder.bind(notiQueue2).to(exchange).with(RabbitMQConstant.RoutingKeys.ROUTING_KEY_V2);

        return new Declarables(
                dlx, notiDLQ1, notiDLQ2, notiDLQ1Binding, notiDLQ2Binding,
                exchange, notiQueue1, notiQueue2, notiQueue1Binding, notiQueue2Binding
        );
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
