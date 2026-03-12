package vduczz.userservice.infrastructure.config.messagequeue.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
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
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    // ____________________ TypeMapping ____________________ //
    @Bean
    public DefaultJacksonJavaTypeMapper typeMapper() {
        // Bean to create TypeMapper

        // init TypeMapper
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();

        // => ưu tiên sử dụng type header // __TypeId__
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.TYPE_ID);

        // mappings
        Map<String, Class<?>> mappings = new HashMap<>();
        // put(alias, FQN)
        mappings.put(
                KafkaProduceConstants.UserEvents.EventTypes.USER_CREATED_V1,
                WelcomeMailRequest.class
        );
        // put more ...

        // __TypeId__ -> Class
        typeMapper.setIdClassMapping(mappings);

        return typeMapper;
    }

    // Sử dụng TypeMapper trong ProducerFactory
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        // Bean to custom config producer - ProducerFactory

        // sử dụng các config có sẵn trong application.yaml
        Map<String, Object> configProps = kafkaProperties.buildProducerProperties(); // chỉ sử dụng các thuộc tính cho producer

        // init serializer
        JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>();
        serializer.setTypeMapper(typeMapper()); // addTypeMapper -> spring.json.type.mapper
        serializer.setAddTypeInfo(true); // addTypeInfo -> spring.json.add.type.headers
        // nguyên tắc: 1 thuộc tính chỉ có thể được khởi tạo giá trị thông qua:
        //      + serializer.set...()
        //      + hoặc, config trong application.yaml
        // tức là chỉ setABC() hoặc config abc trong yaml, không được cả 2
        // example
        configProps.remove("spring.json.add.type.headers");
        configProps.remove("spring.json.type.mapping");


        return new DefaultKafkaProducerFactory<>(
                configProps, // config có sẵn trong yaml
                new StringSerializer(), // key serializer
                serializer // value serializer
        );
    }

    @Bean
    // sử dụng custom ProducerFactory cho KafkaTemplate
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    // ____________________ Create Topic ____________________ //
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
