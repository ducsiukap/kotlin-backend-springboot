package vduczz.notificationservice.config

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.ExchangeBuilder
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
class RabbitMQConfig {

    // ============================================================
    // (Optional) Listener from remote service queue
    // phía listener có thể config các queue mà nó connect tới producer
    @Bean
    fun listenDeadLetterQueue(): Queue =
        QueueBuilder
            .durable(RabbitMQConstants.ListenerQueue.USER__NOTIFICATION_WELCOME_DLQ)
            .build()

    @Bean
    fun listenNotificationWelcomeQueue(): Queue =
        QueueBuilder
            .durable(RabbitMQConstants.ListenerQueue.USER__NOTIFICATION_WELCOME_QUEUE)
            .withArgument("x-dead-letter-exchange", RabbitMQConstants.ListenerExchange.USER__USER_EVENTS_DLX)
            .withArgument("x-dead-letter-routing-key", RabbitMQConstants.ListenerRoutingKey.USER__USER_CREATED)
            .build()

    @Bean
    fun listenDeadLetterExchange(): TopicExchange =
        TopicExchange(RabbitMQConstants.ListenerExchange.USER__USER_EVENTS_DLX)

    @Bean
    fun listenUserEventsExchange(): TopicExchange =
        TopicExchange(RabbitMQConstants.ListenerExchange.USER__USER_EVENTS_EXCHANGE)

    @Bean
    fun listenDlqBinding(
        listenDeadLetterQueue: Queue,
        listenDeadLetterExchange: TopicExchange,
    ): Binding =
        BindingBuilder
            .bind(listenDeadLetterQueue)
            .to(listenDeadLetterExchange)
            .with(RabbitMQConstants.ListenerRoutingKey.USER__USER_CREATED)

    @Bean
    fun listenNotificationWelcomeBinding(
        listenNotificationWelcomeQueue: Queue,
        listenUserEventsExchange: TopicExchange
    ): Binding =
        BindingBuilder
            .bind(listenNotificationWelcomeQueue)
            .to(listenUserEventsExchange)
            .with(RabbitMQConstants.ListenerRoutingKey.USER__USER_CREATED)

    // ============================================================
    // Required
    // + nếu là producer cho service khác -> cần config Queue + Exchange + Binding
    // ------------------------------------------------------------
    // + MessageConverter
    @Bean
    fun jsonMessageConverter(): MessageConverter {
        return JacksonJsonMessageConverter();
    }

    // ------------------------------------------------------------
    // + ack-mode=auto => cần cấu hình MessageRecoverer để gửi message vào DLQ
    //                      tránh MessageRecoverer mặc định của Spring bỏ qua message
    //      // Khi vượt quá retries (max-retries), Spring tự gọi MessageRecoverer để xử lý
    // + ack-mode=manual thì có thể không cần
    @Bean
    fun messageRecoverer(amqpTemplate: AmqpTemplate): MessageRecoverer {

        // RepublishMessageRecoverer nhận exception stacktrace và tự đưa vào message's header:
        //      x-exception-message
        //      x-exception-stacktrace
        // sau đó, truyền letter vào dlx
        return RepublishMessageRecoverer(
            amqpTemplate,

            // trùng với withArgument bên phía produce
            RabbitMQConstants.ListenerExchange.USER__USER_EVENTS_DLX, // DLX (DLQ Exchange)
            RabbitMQConstants.ListenerRoutingKey.USER__USER_CREATED // ROUTING_KEY
        )
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