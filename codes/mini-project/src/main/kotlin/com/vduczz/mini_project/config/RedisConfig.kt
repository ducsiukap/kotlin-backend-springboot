package com.vduczz.mini_project.config

import org.springframework.cache.Cache
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.lang.RuntimeException
import java.time.Duration

@Configuration
@EnableCaching // required => cho phép Caching toàn app
class RedisConfig : CachingConfigurer {
    // extends CachingConfigurer để hỗ trợ errorHandler

    // Redis cache config
    // common config for all table
    @Bean
    fun cacheConfiguration(): RedisCacheConfiguration {

        val config = RedisCacheConfiguration.defaultCacheConfig()
            // Cache TTL
            .entryTtl(Duration.ofMinutes(60)) // -> data được lưu trong RAM trong 60'

            // null value
            .disableCachingNullValues() // không cache null

            // key
            .serializeKeysWith( // lưu key dạng String
                RedisSerializationContext
                    .SerializationPair
                    .fromSerializer(RedisSerializer.string()) // string
            )

            // value
            .serializeValuesWith( // lưu key dạng json
                RedisSerializationContext
                    .SerializationPair
                    .fromSerializer(RedisSerializer.json()) // json
            )

        return config
    }

    // ============================================================
    // custom cache cho từng loại
    //    @Bean
    //    fun cacheConfiguration1(connectionFactory: RedisConnectionFactory): RedisCacheManager {
    //
    //        val configUser = RedisCacheConfiguration.defaultCacheConfig()
    //            .entryTtl(Duration.ofMinutes(20))
    //            .disableCachingNullValues()
    //
    //        val configProduct = RedisCacheConfiguration.defaultCacheConfig()
    //            .entryTtl(Duration.ofMinutes(30))
    //
    //        //        return RedisCacheManager.builder(connectionFactory)
    //        //            .cacheDefaults(configUser)
    //        //            .build() // giống config bên trên (common config for all table)
    //
    //        // custom config ttl
    //        return RedisCacheManager.builder(connectionFactory)
    //            .withCacheConfiguration("users", configUser) // custom cache config for "users"
    //            .withCacheConfiguration("products", configProduct) // custom cache config for "products"
    //            .build() // build
    //    }


    // ============================================================
    // Caching error handler
    override fun errorHandler(): CacheErrorHandler {
        return object : CacheErrorHandler {

            override fun handleCacheGetError(exception: RuntimeException, cache: Cache, key: Any) {
                //log
                println("[CacheErrorHandler] GET error: ${exception.message}")
            }

            override fun handleCachePutError(exception: RuntimeException, cache: Cache, key: Any, value: Any?) {
                println("[CacheErrorHandler] PUT error: ${exception.message}")
            }

            override fun handleCacheEvictError(exception: RuntimeException, cache: Cache, key: Any) {
                println("[CacheErrorHandler] EVICT error: ${exception.message}")
            }

            override fun handleCacheClearError(exception: RuntimeException, cache: Cache) {
                println("[CacheErrorHandler] CLEAR error: ${exception.message}")
            }
        }
    }

}