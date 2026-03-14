package vduczz.userservice.infrastructure.config.messagequeue.rabbitmq;

public class RabbitMQConstant {
    private RabbitMQConstant() {
    }

    public static class Exchanges {
        private Exchanges() {
        }

        public static final String EXCHANGE = "user.events.exchange";
        public static final String DLX = "user.events.dlx";
    }

    public static class RoutingKeys {
        private RoutingKeys() {
        }

        public static final String ROUTING_KEY_V1 = "user.created.v1";
        public static final String ROUTING_KEY_V2 = "user.created.v2";
    }
}
