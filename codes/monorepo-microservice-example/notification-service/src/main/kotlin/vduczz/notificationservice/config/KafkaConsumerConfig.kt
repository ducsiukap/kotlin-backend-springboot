package vduczz.notificationservice.config

import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import vduczz.notificationservice.controller.dto.WelcomeMailRequest
import vduczz.notificationservice.controller.dto.WelcomeMailRequest2

@Configuration
class KafkaConsumerConfig(
    private val kafkaProperties: KafkaProperties
) {

    /* ========================================
    * TYPE-MAPPING KAFKA LISTENERS // JSON DESERIALIZE
    * MAPPINGS VIA __TypeId__ (or x-event-type)
    ======================================== */
    // -------------------------
    // BEAN TO CREATE MAPPING TypeHeader <-> Class
    @Bean
    fun typeMapper(): DefaultJacksonJavaTypeMapper {
        val typeMapper = DefaultJacksonJavaTypeMapper()
        typeMapper.typePrecedence = JacksonJavaTypeMapper.TypePrecedence.TYPE_ID
        typeMapper.classIdFieldName =  "x-event-type"

        val mappings = HashMap<String, Class<*>>()
        mappings[KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V1] = WelcomeMailRequest::class.java
        mappings[KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V2] = WelcomeMailRequest2::class.java

        typeMapper.idClassMapping = mappings
        typeMapper.addTrustedPackages("*")
        return typeMapper
    }

    // -------------------------
    // BEAN APPLY TYPE-MAPPER TO ConsumerFactory
    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        // auto-config Kafka properties for consumer
        val configProps = kafkaProperties.buildConsumerProperties()

        val deserializer = JacksonJsonDeserializer<Any>();
        deserializer.typeMapper = typeMapper() // spring.json.type.mapping
        // trong TypeMapper đã config setTrustedPackages sẵn // spring.json.trusted.packages
        deserializer.setUseTypeHeaders(true) // spring.json.use.type.headers
        // cần xóa auto-config để tránh conflict
        configProps.remove("spring.json.use.type.headers");
        configProps.remove("spring.json.type.mapping");
        configProps.remove("spring.json.trusted.packages");

        return DefaultKafkaConsumerFactory<String, Any>(
            configProps,
            StringDeserializer(),
            deserializer,
        )
    }

    // -------------------------
    // BEAN CUSTOM CONFIG LISTENER -> APPLY custom ConsumerFactory
    @Bean
    @Primary
    fun kafkaListenerContainerFactory(
        // những thứ đã config trong yaml
        // vì đã custom nên nó sẽ inject consumerFactory bên trên
        consumerFactory: ConsumerFactory<String, Any>
    ): ConcurrentKafkaListenerContainerFactory<String, Any> {

        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()

        // gắn consumer factory
        factory.setConsumerFactory(consumerFactory)

        // thêm các cấu hình nâng cao
        // hoặc tùy chỉnh các cấu hình
        factory.setConcurrency(3) // thread consumer concurrency
        return factory
    }

    /* ========================================
    * Bean create value-deserializer = StringDeserialize
    * For ConsumerFactory
    ======================================== */
    @Bean("stringKafkaListenerContainerFactory")
    fun stringKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {

        // ConsumerFactory
        val consumerProps = kafkaProperties.buildConsumerProperties()
        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(
            consumerProps,
            StringDeserializer(),
            StringDeserializer(),
        )

        // KafkaListenerContainerFactory
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.setConsumerFactory(consumerFactory)

        // có thể config auto convert Json -> String tại đây
        // nhưng nếu vậy thì bỏ qua event_type => không nên
        // factory.setRecordMessageConverter(StringJacksonJsonMessageConverter());

        return factory
    }
}
