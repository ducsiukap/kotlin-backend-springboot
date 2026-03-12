package vduczz.notificationservice.config

import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import vduczz.notificationservice.controller.dto.WelcomeMailRequest

@Configuration
class KafkaConsumerConfig(
    private val kafkaProperties: KafkaProperties
) {
    // ____________________ Customer TypeMapper (JSON - Deserializer) ____________________ //
    @Bean
    fun idMappings(): DefaultJacksonJavaTypeMapper {
        // Bean tạo TypeMapper

        val typeMapper = DefaultJacksonJavaTypeMapper()
        typeMapper.typePrecedence = JacksonJavaTypeMapper.TypePrecedence.TYPE_ID

        val mappings = HashMap<String, Class<*>>()
        mappings[KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V1] = WelcomeMailRequest::class.java

        typeMapper.idClassMapping = mappings
        typeMapper.addTrustedPackages("*")
        return typeMapper
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        // Bean custom ConsumerFactory
        // gắn TypeMapper vào ConsumerFactory

        // auto-config Kafka properties for consumer
        val configProps = kafkaProperties.buildConsumerProperties()

        // init deserializer
        val deserializer = JacksonJsonDeserializer<Any>();
        deserializer.typeMapper = idMappings() // spring.json.type.mapping
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


    // tùy chỉnh config ListenerContainer
    @Bean
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

        // convert string -> JSON (for String Deserializer)
        //        factory.setRecordMessageConverter(
        //            // sử dụng có sẵn của spring
        //            StringJacksonJsonMessageConverter()
        //        )

        return factory
    }
}
