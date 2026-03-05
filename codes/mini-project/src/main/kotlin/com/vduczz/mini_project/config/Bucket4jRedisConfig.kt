package com.vduczz.mini_project.config

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class Bucket4jRedisConfig(

    // có thể dùng chung connection tới redis server của server
    // private val redisConnectionFactory: RedisConnectionFactory,
) {

    // hoặc tạo new connection to redis server
    // redisClientForBucket() tạo ra connection khác, dành riêng cho Bucker4j
    @Bean
    fun redisClientForBucket(
        @Value("\${spring.data.redis.host}") redisHost: String,
        @Value("\${spring.data.redis.port}") redisPort: Int,
        @Value("\${spring.data.redis.timeout}") redisTimeout: Duration,
    ): RedisClient {
        // mặc định: 127.0.0.1:6370
        // nếu có pass: redis://password@localhost:6379

        val redisUri = RedisURI.builder()
            .withHost(redisHost)
            .withPort(redisPort)
            .withTimeout(Duration.ofMillis(redisTimeout.toMillis()))
            .build()

        return RedisClient.create(redisUri)
    }


    // cấp proxy tới redis client
    @Bean
    fun proxyManager(redisClient: RedisClient): ProxyManager<ByteArray> {
        return Bucket4jLettuce.casBasedBuilder(redisClient)
            .expirationAfterWrite(
                // xóa key sau 1'
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1))
            )
            .build()
    }

}