package vduczz.notificationservice.config

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Declarables
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.retry.MessageRecoverer
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitListenerFromUserConfig {

    // ============================================================
    // Listener nên là người tạo Queue và Binding qua Exchange và Key của producer
    // Tuy nhiên, khuyến khích cả 2 bên binding
    // ============================================================
    // Main Queue
    companion object {
        const val QUEUE_V1 = "notification.welcome.queue.v1";
        const val QUEUE_V2 = "notification.welcome.queue.v2";
        const val EXCHANGE = "user.events.exchange";

        // ============================================================
        // DLQ - Dead Letter Queue
        const val DLQ_NOTIFICATION_V1 = "notification.welcome.dlq.v1";
        const val DLQ_NOTIFICATION_V2 = "notification.welcome.dlq.v2";
        const val DLX_NAME = "user.events.dlx";

        // ============================================================
        // Common routing key
        const val ROUTING_KEY_V1 = "user.created.v1";
        const val ROUTING_KEY_V2 = "user.created.v2";
    }

    /* ========================================
    * Declarables: xử lý khi quá nhiều queue cần map
    ======================================== */
    @Bean
    fun userQueueBinding(): Declarables {
        val notiDLQ1 = Queue(DLQ_NOTIFICATION_V1, true);
        val notiDLQ2 = Queue(DLQ_NOTIFICATION_V2, true);
        val dlx = TopicExchange(DLX_NAME);
        val notiDLQ1Binding = BindingBuilder.bind(notiDLQ1).to(dlx).with(ROUTING_KEY_V1);
        val notiDLQ2Binding = BindingBuilder.bind(notiDLQ2).to(dlx).with(ROUTING_KEY_V2);

        val notiQueue1 = QueueBuilder.durable(QUEUE_V1)
            .withArgument("x-dead-letter-exchange", DLX_NAME)
            .withArgument("x-dead-letter-routing-key", ROUTING_KEY_V1)
            .build();
        val notiQueue2 = QueueBuilder.durable(QUEUE_V2)
            .withArgument("x-dead-letter-exchange", DLX_NAME)
            .withArgument("x-dead-letter-routing-key", ROUTING_KEY_V2)
            .build();
        val exchange = TopicExchange(EXCHANGE);
        val notiQueue1Binding = BindingBuilder.bind(notiQueue1).to(exchange).with(ROUTING_KEY_V1);
        val notiQueue2Binding = BindingBuilder.bind(notiQueue2).to(exchange).with(ROUTING_KEY_V2);

        return Declarables(
            dlx, notiDLQ1, notiDLQ2, notiDLQ1Binding, notiDLQ2Binding,
            exchange, notiQueue1, notiQueue2, notiQueue1Binding, notiQueue2Binding
        );
    }

    // ------------------------------------------------------------
    // (Optional) Có thể custom SimpleRabbitListenerContainerFactory
    //  mặc định khi đã khai báo về listener trong application.yaml thì không cần nữa
    //    @Bean
    //    fun rabbitListenerContainerFactory(
    //        connectionFactory: ConnectionFactory, // ConnectionFactory
    //        jsonMessageConverter: MessageConverter // MessageConverter
    //    ): SimpleRabbitListenerContainerFactory =
    //        SimpleRabbitListenerContainerFactory().apply {

    //            setConnectionFactory(connectionFactory)
    //            setMessageConverter(jsonMessageConverter)

    //            setAcknowledgeMode(AcknowledgeMode.MANUAL) // ack-mode

    //            // tương tự application.taml
    //            setPrefetchCount(10)
    //            setConcurrentConsumers(3)
    //            setMaxConcurrentConsumers(10)
    //        }
}