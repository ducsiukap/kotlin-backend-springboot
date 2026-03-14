package vduczz.notificationservice.controller.dto

import java.time.Instant
import java.util.UUID

data class WelcomeMailRequest(
    val email: String,
    val name: String
)


data class WelcomeMailRequest2(
    val id: UUID,
    val name: String,
    val email: String,
    val occurredOn: Instant
)