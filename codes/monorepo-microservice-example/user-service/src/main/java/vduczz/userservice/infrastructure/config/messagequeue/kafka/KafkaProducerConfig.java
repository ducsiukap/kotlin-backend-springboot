package vduczz.userservice.infrastructure.config.messagequeue.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper;
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import vduczz.userservice.infrastructure.client.dto.WelcomeMailRequest;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    //    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServer;
//
//    @Bean
//    public ProducerFactory<String, Object> producerFactory() {
//        Map<String, Object> config = new HashMap<>();
//
//        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
//        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializable.class);
//        config.put(ProducerConfig.ACKS_CONFIG, "all");
//        config.put(ProducerConfig.RETRIES_CONFIG, 3);
//        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // chống duplicate message
//
//        return new DefaultKafkaProducerFactory<>(config);
//    }
//
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }


    // ____________________ TypeMapping ____________________ //
    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {

        JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>();

        serializer.setTypeMapper(typeMapper());
        serializer.setAddTypeInfo(true);

        Map<String, Object> configProps = kafkaProperties.buildProducerProperties();
        configProps.remove("spring.json.add.type.headers");
        configProps.remove("spring.json.type.mapping");

        return new DefaultKafkaProducerFactory<>(
                configProps,
                new StringSerializer(),
                serializer // phải bỏ value-serializer trong yaml :)
        );
    }

    @Bean
    public DefaultJacksonJavaTypeMapper typeMapper() {

        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();

        // Ưu tiên header hơn default type
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.TYPE_ID);

        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put(
                KafkaProduceConstants.UserEvents.EventTypes.USER_CREATED_V1,
                WelcomeMailRequest.class
        );
        // put more ...

        typeMapper.setIdClassMapping(mappings);
        return typeMapper;
    }

    // Tạo topic tự động với partition và replication factor
    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder
                .name(KafkaProduceConstants.UserEvents.USER_EVENTS_TOPIC)
                .partitions(3) // 3 partition -> parallel processing
                .replicas(1) // 1 replica
                .build();
    }
}
