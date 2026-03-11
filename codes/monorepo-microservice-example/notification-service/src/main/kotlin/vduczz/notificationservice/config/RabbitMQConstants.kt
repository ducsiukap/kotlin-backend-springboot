package vduczz.notificationservice.config

object RabbitMQConstants {

    object ListenerQueue {
        const val USER__NOTIFICATION_WELCOME_QUEUE = "notification.welcome.queue"
        const val USER__NOTIFICATION_WELCOME_DLQ = "notification.welcome.queue.dlq"
    }

    object ListenerExchange {
        const val USER__USER_EVENTS_EXCHANGE = "user.events.exchange"
        const val USER__USER_EVENTS_DLX = "user.events.dlx"
    }

    object ListenerRoutingKey {
        const val USER__USER_CREATED = "user.created"
    }

}