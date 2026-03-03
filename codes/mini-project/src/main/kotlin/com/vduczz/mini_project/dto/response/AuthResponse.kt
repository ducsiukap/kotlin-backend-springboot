package com.vduczz.mini_project.dto.response

import java.util.UUID

data class AuthResponse(
    val userId: UUID,
    val token: String,
)