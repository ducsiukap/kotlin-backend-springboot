package com.vduczz.api_gateway.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/fallback")
class FallbackController {

    @RequestMapping("/order-service")
    suspend fun orderFallback(exchange: ServerWebExchange) =
        mapOf(
            "status" to HttpStatus.SERVICE_UNAVAILABLE,
            "error" to "Order Service is temporary unavailable",
            "message" to "Please try again later",
            "path" to (exchange.request.path.value())
        ).also {
            exchange.response.statusCode = HttpStatus.SERVICE_UNAVAILABLE
        }

    @RequestMapping("/payment-service")
    suspend fun paymentFallback(exchange: ServerWebExchange) =
        mapOf(
            "status" to HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error" to "Payment service is temporarily unavailable",
            "message" to "Your order is saved. Payment will be retried automatically"
        ).also {
            exchange.response.statusCode = HttpStatus.SERVICE_UNAVAILABLE
        }
}