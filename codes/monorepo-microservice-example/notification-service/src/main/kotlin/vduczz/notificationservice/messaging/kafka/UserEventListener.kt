package vduczz.notificationservice.messaging.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import vduczz.notificationservice.config.KafkaConsumeConstants
import vduczz.notificationservice.controller.dto.WelcomeMailRequest
import vduczz.notificationservice.service.NotificationService

@Component
// Multiple message-type in one topic
@KafkaListener(
    topics = [KafkaConsumeConstants.UserEvents.USER_EVENTS_TOPIC],
    groupId = KafkaConsumeConstants.GROUP_ID,
//    containerFactory = "",
)
class UserEventListener(
    private val notificationService: NotificationService
) {

    @KafkaHandler // handle 1 loại message type
    fun handleUserCreateEvent(
        @Payload
        request: WelcomeMailRequest, // JSON parse chính xác -> match
    ) {
        println("check")
        notificationService.sendWelcomeMail(request);
    }

    // ____________________ Default Handler ____________________ //
    // khi JSON parse không match request nào
    // bắt buộc hứng để tránh lỗi
    //  => sử dụng cho các message mà nó không quan tâm
    @KafkaHandler(isDefault = true)
    fun handleUnknowMessaage(
        @Payload
        payload: Any, // payload only

        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Header(KafkaHeaders.OFFSET) offset: Long,

        // full-message
        message: ConsumerRecord<String, Any>
    ) {
        // do smth
        println("Hello World :)")
        println(payload)
        println("${payload::class.java.name}")
    }
}