package com.vduczz.api_gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String = "",
)

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class AppConfig