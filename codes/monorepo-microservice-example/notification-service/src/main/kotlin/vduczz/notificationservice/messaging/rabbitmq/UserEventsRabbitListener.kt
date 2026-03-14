package vduczz.notificationservice.messaging.rabbitmq

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import vduczz.notificationservice.config.RabbitListenerFromUserConfig
import vduczz.notificationservice.controller.dto.WelcomeMailRequest
import vduczz.notificationservice.controller.dto.WelcomeMailRequest2
import vduczz.notificationservice.service.NotificationService

@Component // Bean
class UserEventsRabbitListener(
    private val notificationService: NotificationService
) {


    // Hứng message từ Main-Queue
    // ============================================================
    // Tương tự @Controller nhưng không sử dụng @Controller
    // Sử dụng @RabbitListener
    @RabbitListener(
        queues = [
            RabbitListenerFromUserConfig.QUEUE_V1// Nên config queue ở application.yaml
        ]
    )
    fun handleUserCreateEvent(
        @Payload // (Optional) nếu chỉ có 1 tham số -> mặc định là @Payload
        // có thể dùng @Valid để validate
        request: WelcomeMailRequest,

        // nếu nhiều tham số => thì còn lại là metadata
        // + Header
        // @Header("amqp_receivedRoutingKey") routingKey: String, // Routing Key
        // @Header(AmqpHeaders.CONSUMER_QUEUE) // queue
        // @Header("X-Correlation-ID") traceId: String?, // Custom Header gửi từ bên UserService (nếu có)
        // @Header headers: Map<String, Any> // toàn bộ header

        // + Message -> gói tin thô: chứa cả byte[] body và MessageProperties
        message: Message, // org.springframework.amqp.core.Message,
        // + Channel -> manual ack-mode
        channel: Channel  //com.rabbitmq.client.Channel
    ) {
        println("RabbitMQ")
        // ack-mode: AUTO
        notificationService.sendWelcomeMail(request = request)
        // + kết thúc hàm không gặp lỗi => ACK
        // + kết thúc hàm gặp lỗi -> NACK + requeue
        //      => để tránh requeue infinite loop, cần kết hợp config max-retires
    }

    @RabbitListener(
        queues = [
            RabbitListenerFromUserConfig.QUEUE_V2// Nên config queue ở application.yaml
        ]
    )
    fun handleUserCreateEvent(
        request: WelcomeMailRequest2,
    ) {
        println("RabbitMQ-2")
//        notificationService.sendWelcomeMail(request = request)
    }

    // Hứng message từ DLQ (optional)
    // ============================================================
    @RabbitListener(
        // sử dụng Queue & Binding đã config
        queues = [
            RabbitListenerFromUserConfig.DLQ_NOTIFICATION_V1
        ]

        // (Optional)
        // tự tạo binding thông qua bindings
        //      config Queue + Exchange + Binding
        //      tương tự trong RabbitMQConfig mà không cần viết RabbitMQConfig
        // import từ org.springframework.amqp.rabbit.annotation.QueueBing, Queue, ...
        //        bindings = [QueueBinding(
        //            value = Queue(
        //                value = "notification.welcome.dlq",
        //                durable = "true"
        //            ), // import org.springframework.amqp.rabbit.annotation.Queue
        //            // không phải import org.springframework.amqp.core.Queue
        //            exchange = Exchange(value = "user.events.dlx", type = "topic"),
        //            key = ["user.created"]
        //        )]
    )
    fun handleDeadLetter(failedMessage: Message) {
        val reason = failedMessage.messageProperties.headers["x-exception-message"];
        // val trace = failedMessage.messageProperties.headers["x-exception-stacktrace"];

        // log...
        println("Failed Message: $reason")
    }

    @RabbitListener(
        queues = [
            RabbitListenerFromUserConfig.DLQ_NOTIFICATION_V2
        ]
    )
    fun handleDeadLetter2(failedMessage: Message) {
        val reason = failedMessage.messageProperties.headers["x-exception-message"];
        // log...
        println("Failed Message: $reason")
    }
}