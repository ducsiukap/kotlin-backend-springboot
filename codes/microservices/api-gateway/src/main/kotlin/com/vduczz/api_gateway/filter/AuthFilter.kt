package com.vduczz.api_gateway.filter

import com.vduczz.api_gateway.util.JwtUlti
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

// AbstractGatewayFilterFactory -> custom filter theo từng route
@Component
class AuthFilter(
    private val jwtUtil: JwtUlti
) : AbstractGatewayFilterFactory<AuthFilter.Config>(Config::class.java) {

    /* ========================================
    // Config -- cho phép customize per-route
    ======================================== */
    data class Config(
        // -> yêu cầu role cho route
        val requiredRole: String? = null
        // null -> chỉ cần login
        // ADMIN, ...
    )

    // Khai báo shortcut name trong YAML: "AuthFilter"
    // Cho phép dùng đúng tên làm filter trong YAML
    override fun name(): String = "AuthFilter"


    /* ========================================
    * FILTER
    ======================================== */
    override fun apply(config: Config): GatewayFilter =
        GatewayFilter { exchange, chain ->

            // extract auth header from request
            val authHeader = exchange.request.headers
                .getFirst(HttpHeaders.AUTHORIZATION)

            // thiếu token -> 401 / token không đúng
            if (authHeader.isNullOrBlank() ||
                !authHeader.startsWith("Bearer ")
            ) {
                return@GatewayFilter rejectWith(
                    exchange = exchange,
                    status = HttpStatus.UNAUTHORIZED,
                    message = "Missing or invalid Authorization header!"
                )
            }

            val token = authHeader.removePrefix("Bearer ")

            // token không hợp lệ / hết hạn
            if (!jwtUtil.isValidToken(token)) {
                return@GatewayFilter rejectWith(
                    exchange, HttpStatus.UNAUTHORIZED,
                    "Token is invalid or expired!"
                )
            }

            val userId = jwtUtil.extractUserId(token)
            val role = jwtUtil.extractRole(token)

            // role checking
            if (config.requiredRole != null && role != config.requiredRole) {
                return@GatewayFilter rejectWith(
                    exchange, HttpStatus.FORBIDDEN,
                    "Insufficient permission!"
                )
            }

            // token OK
            val mutatedRequest = exchange.request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                // xóa auth header (optional)
                .headers { it.remove(HttpHeaders.AUTHORIZATION) }
                .build()

            // next filter chain
            chain.filter(
                exchange.mutate().request(mutatedRequest).build()
            )
        }

    // util method
    private fun rejectWith(
        exchange: ServerWebExchange,
        status: HttpStatus,
        message: String
    ): Mono<Void> {
        val response = exchange.response
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON

        val body = """
            {
                "error": "$message",
                "status": ${status.value()}
            }
            """.trimIndent()

        // WebFlux là Reactive, không ghi String trực tiếp
        // Cần convert sang DataBuffer
        val buffer = response.bufferFactory().wrap(body.toByteArray(Charsets.UTF_8))

        // response.writeWith
        //  + ghi body vào response
        //  + kết thúc request
        return response.writeWith(Mono.just(buffer))
    }
}