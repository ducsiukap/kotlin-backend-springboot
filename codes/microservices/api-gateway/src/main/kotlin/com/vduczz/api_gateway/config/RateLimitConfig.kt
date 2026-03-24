package com.vduczz.api_gateway.config

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import reactor.core.publisher.Mono

@Configuration
class RateLimitConfig {

    @Bean
    fun userKeyResolver(): KeyResolver = KeyResolver { exchange ->
        val userId = exchange.request.headers
            .getFirst("X-User-Id") ?: "annonymous"

        Mono.just(userId)
    }

    @Bean
    @Primary
    fun ipKeyResolver(): KeyResolver = KeyResolver { exchange ->
        val ip = exchange.request
            .remoteAddress?.address?.hostAddress ?: "unknow"
        Mono.just(ip)
    }
}