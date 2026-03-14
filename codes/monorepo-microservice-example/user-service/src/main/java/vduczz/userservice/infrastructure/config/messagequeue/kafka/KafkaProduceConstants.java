package vduczz.userservice.infrastructure.config.messagequeue.kafka;

public final class KafkaProduceConstants {
    private KafkaProduceConstants() {
    }

    public static final class UserEvents {
        private UserEvents() {
        }

        public static final String USER_EVENTS_TOPIC = "user.events";

        public static final class EventTypes {
            public static final String USER_CREATED_V1 = "user.created.v1";
            public static final String USER_CREATED_V2 = "user.created.v2";
        }
    }

}
