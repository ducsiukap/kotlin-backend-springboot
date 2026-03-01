package com.vduczz.mini_project.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    val securitySchemeName = "Bearer Authentication"

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info( // information
                Info().title("KSB")
                    .description("Kotlin-Springboot BE mini project...")
                    .version("v1.0.0")
                    .contact(Contact().name("vduczz").email("DucPV.contact@gmail.com"))
            )
            // 2. CẮM Ổ KHÓA BẢO MẬT (Chuẩn bị cho Chương 2: Spring Security)
            // Cấu hình nút "Authorize" hiện lên trên góc phải màn hình
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))//
            .components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme().name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT") // sử dụng JWT Token
                )
            )
    }
}