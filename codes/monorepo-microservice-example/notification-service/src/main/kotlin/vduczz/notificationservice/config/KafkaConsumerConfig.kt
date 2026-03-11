package vduczz.notificationservice.config

import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import vduczz.notificationservice.controller.dto.WelcomeMailRequest

@Configuration
class KafkaConsumerConfig {

    // >>>>>>>>>>>>>>>>>>>>>>>>> Generic Deserializer + TypeMapper <<<<<<<<<<<<<<<<<<<<<<<<< //
    // ____________________ Khi muốn can thiệp sâu vào consumer ____________________ //
    // -> cấu hình tương tự bên producer

    @Bean
    fun consumerFactory(kafkaProperties: KafkaProperties): ConsumerFactory<String, Any> {

        println("=========> MY consumerFactory LOADED")

        // Nếu trong code config rồi thì không được khai báo trong application.yaml
        // ex: value-deserializer , spring.json.use.type.header, spring.json.trusted.packages
        //      type-mapper, key-serializer
        val deserializer = JacksonJsonDeserializer<Any>().apply {
            setUseTypeHeaders(true)
            addTrustedPackages("*")

            typeMapper = typeMapper()
        }

        val configProps = kafkaProperties.buildConsumerProperties()
//        configProps.remove("spring.json.trusted.packages")
//        configProps.remove("spring.json.use.type.header")

        return DefaultKafkaConsumerFactory<String, Any>(
            configProps,
            StringDeserializer(),
            ErrorHandlingDeserializer(deserializer)
        )
    }

    @Bean
    fun typeMapper(): DefaultJacksonJavaTypeMapper {

        val mappings: MutableMap<String, Class<*>> = HashMap()
        mappings[KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V1] = WelcomeMailRequest::class.java

        val typeMapper = DefaultJacksonJavaTypeMapper().apply {
            typePrecedence = JacksonJavaTypeMapper.TypePrecedence.TYPE_ID
            idClassMapping = mappings
        }

        return typeMapper
    }

    @Bean
    // Bean ép KafkaListenerContainerFactory dùng consumerFactory bên trên :)
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, Any>
    ): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.setConsumerFactory(consumerFactory)
        return factory
    }

    // khi chỉ muốn can thiệp TypeMapper + Deserializer / Serializer


    // @Bean
    // sử dụng Kafka RecordMessageConverter //
    // Spring sẽ xử lý phần Error Handling và Conversion
    // một cách thông minh hơn ở tầng cao .// khi này cần set value deserializer là StringDeserializer
    //    fun kafkaJsonMessageConverter(): RecordMessageConverter {
    //        val converter = JacksonJsonMessageConverter()
    //        val typeMapper = DefaultJacksonJavaTypeMapper()
    //
    //        typeMapper.typePrecedence = JacksonJavaTypeMapper.TypePrecedence.TYPE_ID
    //
    //        // id mapping
    //        val mappings = HashMap<String, Class<*>>()
    //        mappings[KafkaConsumeConstants.UserEvents.EventTypes.USER_CREATED_V1] = WelcomeMailRequest::class.java
    //
    //        typeMapper.idClassMapping = mappings
    //        converter.typeMapper = typeMapper
    //        return converter
    //    }
}
