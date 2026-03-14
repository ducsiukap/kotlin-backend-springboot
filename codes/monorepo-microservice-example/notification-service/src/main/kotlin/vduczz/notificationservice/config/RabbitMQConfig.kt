package vduczz.notificationservice.config

import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.retry.MessageRecoverer
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant

@Configuration
class RabbitMQConfig {
    // ============================================================
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

        return MessageRecoverer { message, cause ->
            println("Message recovery triggered. Cause: ${cause.message}")

            val props = message.messageProperties
            props.setHeader("x-exception-message", cause.message ?: "Unknown")
            props.setHeader("x-exception-stacktrace", cause.stackTrace.joinToString("\n"))
            props.setHeader("x-failed-at", Instant.now())

            // Reject → RabbitMQ tự route sang DLX theo x-dead-letter-exchange đã cấu hình
            throw AmqpRejectAndDontRequeueException(cause.message!!, cause)
        }
    }
}