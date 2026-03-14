package vduczz.notificationservice.config

object KafkaConsumeConstants {
    object UserEvents {
        const val USER_EVENTS_TOPIC = "user.events"

        object EventTypes {
            const val USER_CREATED_V1 = "user.created.v1"
            const val USER_CREATED_V2 = "user.created.v2"
        }
    }

    const val GROUP_ID = "notification-service-group"
}