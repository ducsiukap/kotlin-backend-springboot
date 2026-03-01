package com.vduczz.mini_project.dto.response

import com.vduczz.mini_project.model.User
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// ------------------------------------------------------------
// Response DTO
data class UserDetailResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val dayOfBirth: LocalDate,
    val status: String,

    // derived của entity nếu cần
    val fullName: String, // fullName thay vì firstName / lastName
    val age: Int, // ex cần cả dob và age, hoặc chỉ 1 trong 2
    val createdAt: LocalDateTime,
)

// Mapping từ Entity -> DTO
// nên đặt ngay dưới DTO
// using extension function map từ entity sang dto
fun User.toUserDetailResponse(): UserDetailResponse {
    return UserDetailResponse(
        id = this.id!!,
        username = this.username,
        email = this.email,
        dayOfBirth = this.dayOfBirth,
        status = this.status.name, // .name: enum -> String
        fullName = "${this.firstName} ${this.lastName}", // derived
        age = this.age,
        createdAt = this.createdAt!!
    )
}