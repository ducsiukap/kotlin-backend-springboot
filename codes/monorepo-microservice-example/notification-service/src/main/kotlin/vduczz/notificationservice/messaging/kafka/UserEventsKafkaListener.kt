package vduczz.notificationservice.messaging.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import vduczz.notificationservice.config.KafkaConsumeConstants
import vduczz.notificationservice.controller.dto.WelcomeMailRequest
import vduczz.notificationservice.controller.dto.WelcomeMailRequest2
import vduczz.notificationservice.service.NotificationService


/* ========================================
* KAFKA HANDLER => JSON DESERIALIZER
======================================== */
@Component
// Multiple message-type in one topic
@KafkaListener(
    topics = [KafkaConsumeConstants.UserEvents.USER_EVENTS_TOPIC],
    groupId = KafkaConsumeConstants.GROUP_ID,
)
class UserEventsKafkaListener(
    private val notificationService: NotificationService
) {

    // ____________________ @Payload Matching Specific Type ____________________ //
    @KafkaHandler // handle 1 loại message type
    fun handleUserCreateEvent(
        @Payload
        request: WelcomeMailRequest, // JSON parse chính xác -> match
    ) {
        // notificationService.sendWelcomeMail(request);
        println("[Kafka] receive event: $request")
    }

    @KafkaHandler
    fun handleUserCreateEvent(
        @Payload
        request: WelcomeMailRequest2,
    ) {
        // notificationService.sendWelcomeMail(request);
        println("[Kafka] receive event: $request")
    }

    // ____________________ Default Handler ____________________ //
    // khi JSON parse không match request nào
    // bắt buộc hứng để tránh lỗi
    //  => sử dụng cho các message mà nó không quan tâm
    @KafkaHandler(isDefault = true)
    fun handleUnknowMessage(
        @Payload
        payload: Any, // payload only

        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Header(KafkaHeaders.OFFSET) offset: Long,

        // full-message
        message: ConsumerRecord<String, Any>
    ) {
        println("[Default Handler] receive event: $message ")
    }
}

/* ========================================
* MANUAL MAPPING -> StringDeserialize
======================================== */
//@Component
//class UserEventsKafkaListener(
//    private val objectMapper: ObjectMapper
//) {
//    @KafkaListener(
//        topics = [KafkaConsumeConstants.UserEvents.USER_EVENTS_TOPIC],
//        groupId = KafkaConsumeConstants.GROUP_ID,
//        containerFactory = "stringKafkaListenerContainerFactory"
//    )
//    fun consume(
//        @Header("__TypeId__") typeId: String?,
//        @Header("x-event-type") eventType: String?,
//        @Payload payload: String
//    ) {
//
//        val type = eventType ?: typeId
//        when (type) {
//            KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V1 -> {
//                val obj = objectMapper.readValue(payload, WelcomeMailRequest::class.java);
//                println("message: $obj")
//
//                // gọi hàm xử lý
//            }
//
//            KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V2 -> {
//                val obj = objectMapper.readValue(payload, WelcomeMailRequest::class.java);
//                println("message: $obj")
//
//            }
//
//            else -> println("Unhandled event type $eventType")
//        }
//    }
//}
