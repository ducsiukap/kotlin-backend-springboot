package com.vduczz.api_gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

// GlobalFilter — chạy cho tất cả request mà     gateway xử lý routing.
// Không bao gồm:
//  + Actuator (có thể bypass)
//  + static resource
// Log request vào, response ra, và gắn Trace ID để trace qua các service.
class LoggingFilter : GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(javaClass)

    // Logging Filter
    override fun filter(
        exchange: ServerWebExchange, // HTTP Request + Response
        chain: GatewayFilterChain // filter chain
    ): Mono<Void> {
        val request = exchange.request
        val traceId = UUID.randomUUID().toString().take(8)
        val startMs = System.currentTimeMillis()

        // -------------------------
        // Previous filter  -> log request
        log.info(
            "[{}] -> {} {}",
            traceId,
            request.method.name(),
            request.uri.path
        )

        // + gắn tractId
        val mutated = exchange.mutate()
            .request { it.header("X-Trace-Id", traceId) }
            .build()

        // -------------------------
        // Post filter
        return chain.filter(mutated) // chuyển đến filter trong
            .then( // thực hiện sau khi response hoàn tất
                Mono.fromRunnable {
                    // runnable: phù hợp logging, metrics, audit
                    // (side-effect)

                    val duration = System.currentTimeMillis() - startMs
                    val status = exchange.response.statusCode?.value() ?: 0

                    log.info(
                        "[{}] <- {} {} ({}ms)",
                        traceId,
                        status,
                        request.uri.path,
                        duration
                    )
                }
            )
    }


    // Order
    // order = -1 → chạy trước tất cả filter khác
    override fun getOrder(): Int = -1
}