package com.vduczz.mini_project.dto.response

import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int, // HTTP status code
    val message: String, // error summary
    val errors: Any? = null // detail error
)