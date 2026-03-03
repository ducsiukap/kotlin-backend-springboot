package com.vduczz.mini_project.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.HandlerTypePredicate
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration // marks with Spring // core-configuration
class WebConfig : WebMvcConfigurer {
    // extends: WebMvcConfigurer

    // ------------------------------------------------------------x
    // config url path
    override fun configurePathMatch(configurer: PathMatchConfigurer) {

        // add prefix
        configurer.addPathPrefix(
            "/api/v1", // uri-prefix

            // prefix cho Annotation
            //HandlerTypePredicate.forAnnotation(RestController::class.java) // Api annotation
            // ex: .forAnnotation(AppApiV1::class.java) , .forAnnotation(AdminApiV1::class.java), ..
            // Khi này, mỗi khi thấy @RestController (or declared API Annotation, such as @AppApiV1, ...)
            //  tự động thêm prefix: "/api/v1

            // hoặc prefix cho controller package
            HandlerTypePredicate.forBasePackage("com.vduczz.mini_project.controller")

        )
    }
}