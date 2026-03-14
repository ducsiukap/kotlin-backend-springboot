package vduczz.userservice.infrastructure.config.messagequeue.kafka;
//

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper;
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import vduczz.userservice.application.port.out.gateway.dto.request.WelcomeMailRequest;
import vduczz.userservice.domain.event.user.AccountCreatedEvent;

import java.util.HashMap;
import java.util.Map;

//
@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    // kafka auto-config in application.yaml
    private final KafkaProperties kafkaProperties;

    /* ========================================
    * CREATE NEW KAFKA TOPIC
    ======================================== */
    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder
                .name(KafkaProduceConstants.UserEvents.USER_EVENTS_TOPIC)
                .partitions(3) // 3 partition -> parallel processing
                .replicas(1) // 1 replica
                .build();
    }


    /* ========================================
    * TYPE-MAPPER for JacksonJsonSerializer
    ======================================== */
    // -------------------------
    // TYPE-MAPPER
    @Bean
    public DefaultJacksonJavaTypeMapper typeMapper() {
        DefaultJacksonJavaTypeMapper typeMapper =
                new DefaultJacksonJavaTypeMapper(); // init TypeMapper
        typeMapper.setTypePrecedence( // => ưu tiên sử dụng type header
                JacksonJavaTypeMapper.TypePrecedence.TYPE_ID // __TypeId__
        );
        typeMapper.setClassIdFieldName("x-event-type"); // Đổi __TypeId__ -> x-event-type

        Map<String, Class<?>> mappings = new HashMap<>(); // mappings alias <-> type
        // put(alias, FQN)
        mappings.put(
                KafkaProduceConstants.UserEvents.EventTypes.USER_CREATED_V1, // alias
                WelcomeMailRequest.class // Type (FQN)
        );
        mappings.put(
                KafkaProduceConstants.UserEvents.EventTypes.USER_CREATED_V2,
                AccountCreatedEvent.class
        );

        typeMapper.setIdClassMapping(mappings); // __TypeId__ -> Class
        return typeMapper;
    }

    // -------------------------
    // BEAN TO CUSTOM ProducerFactory (CUSTOM PRODUCER CONFIG)
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps
                = kafkaProperties.buildProducerProperties(); // auto-config cho producer

        JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>(); // init serializer
        serializer.setTypeMapper(typeMapper()); // addTypeMapper -> spring.json.type.mapper
        serializer.setAddTypeInfo(true); // addTypeInfo -> spring.json.add.type.headers
        // nguyên tắc: 1 thuộc tính chỉ có thể được khởi tạo giá trị thông qua:
        //      + serializer.set...()
        //      + hoặc, config trong application.yaml
        // tức là chỉ setABC() hoặc config abc trong yaml, không được cả 2
        configProps.remove("spring.json.add.type.headers");
        configProps.remove("spring.json.type.mapping");

        return new DefaultKafkaProducerFactory<>(
                configProps, // config có sẵn trong yaml
                new StringSerializer(), // key serializer
                serializer // value serializer
        );
    }

    // -------------------------
    // BEAN TO DEFINE KafkaTemplate BASED ON CUSTOM PRODUCER CONFIG (above)
    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /* ========================================
    * CREATE KafkaTemplate<String, String>  => KHÔNG JsonSerialize, KHÔNG TypeHeader
    ======================================== */
    @Bean("stringKafkaTemplate")
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        var producerProps = kafkaProperties.buildProducerProperties();
        var producerFactory = new DefaultKafkaProducerFactory<>(
                producerProps,
                new StringSerializer(),
                new StringSerializer() // value-serializer
        );
        return new KafkaTemplate<>(producerFactory);
    }
}
