package com.vduczz.api_gateway.config

import com.vduczz.api_gateway.filter.AuthFilter
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//@Configuration
//class RouteConfig(
//    private val authFilter: AuthFilter
//) {
//
//    @Bean
//    fun routeLocator(builder: RouteLocatorBuilder) =
//        builder.routes {
//            /* ========================================
//            * USER SERVICE — public auth endpoints (user service)
//            ======================================== */
//            route(
//                id = "user-auth-route" // id
//            ) {
//                // pedicates
//                path("/api/auth/**")
//
//                // filters
//                // filters {
//                // stripPrefix(1) // -> request gửi tới services sẽ là /auth/**
//                // addRequestHeader("X-Source", "api-gateway")
//                // }
//
//                // uri
//                uri("lb://user-service")
//            }
//
//
//            /* ========================================
//            * PRODUCT SERVICE
//            ======================================== */
//            // - public GET
//            route(id = "product-public-route") {
//                path("/api/products/**")
//                method("GET", "HEAD")
//
//                uri("lb://product-service")
//            }
//            // - protected write
//            route(id = "product-admin-route") {
//                path("/api/products/**")
//                method("POST", "PUT", "PATCH", "DELETE")
//
//                filters {
//                    filter(
//                        authFilter.apply(
//                            AuthFilter.Config(requiredRole = "ADMIN")
//                        )
//                    )
//                }
//
//                uri("lb://product-service")
//            }
//        }
//}