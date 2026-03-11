package vduczz.notificationservice.messaging.rabbitmq

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import vduczz.notificationservice.config.RabbitMQConstants
import vduczz.notificationservice.controller.dto.WelcomeMailRequest
import vduczz.notificationservice.service.NotificationService

@Component // Bean
class UserEventsListener(
    private val notificationService: NotificationService
) {


    // Hứng message từ Main-Queue
    // ============================================================
    // Tương tự @Controller nhưng không sử dụng @Controller
    // Sử dụng @RabbitListener
    @RabbitListener(
        queues = [
            RabbitMQConstants.ListenerQueue.USER__NOTIFICATION_WELCOME_QUEUE// Nên config queue ở application.yaml
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
        // ack-mode: AUTO
        notificationService.sendWelcomeMail(request = request)
        // + kết thúc hàm không gặp lỗi => ACK
        // + kết thúc hàm gặp lỗi -> NACK + requeue
        //      => để tránh requeue infinite loop, cần kết hợp config max-retires

        // example MANUAL ack-mode
        //        tag
        //        val deliveryTag = message.messageProperties.deliveryTag
        //
        //        // số lần retries
        //        val retryCount = message.messageProperties
        //            .headers
        //            .getOrDefault("x-retry-count", 0) as Int
        //
        //        runCatching { // try-catch
        //
        //            // logics, call services, ...
        //
        //            // manual ack confirm
        //            channel.basicAck(
        //                deliveryTag, // deliveryTag: số thứ tự của message
        //
        //                // multiple:
        //                false
        //                // false: chỉ xác nhận message có tag này
        //                // true: xác nhận toàn bộ message chưa ACK tính tới thời điểm tag này
        //            )
        //        }.onFailure { ex ->
        //
        //            ///
        //
        //            // create variable: val maxRetires=3
        //            // if (retryCount < maxRetires) ...
        //            if (retryCount < 3) {
        //                message.messageProperties.headers["x-retry-count"] = retryCount + 1
        //                channel.basicNack(
        //                    deliveryTag, // deliveryTag
        //                    false, // multiple
        //                    true // requeue?
        //                )
        //            } else {
        //                // reaches to max-retries
        //                // ....
        //                channel.basicNack(
        //                    deliveryTag,
        //                    false,
        //                    false // không requeue -> Spring gọi MessageRecoverer -> DLQ
        //                )
        //            }
        //        }
    }

    // Hứng message từ DLQ (optional)
    // ============================================================
    @RabbitListener(
        // sử dụng Queue & Binding đã config
        queues = [
            RabbitMQConstants.ListenerQueue.USER__NOTIFICATION_WELCOME_DLQ
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
}