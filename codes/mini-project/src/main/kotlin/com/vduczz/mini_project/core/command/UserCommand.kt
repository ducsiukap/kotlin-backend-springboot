package com.vduczz.mini_project.core.command

import com.vduczz.mini_project.model.User
import java.time.LocalDate

// POST
data class CreateUserCommand(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
    val email: String,
    val dayOfBirth: LocalDate
)

// Mapping từ Command -> Entity
// nên đặt ngay dưới Command
// using extension function map từ command -> entity
fun CreateUserCommand.toEntity(): User {
    return User(
        firstName = firstName,
        lastName = lastName,
        username = username,
        password = password,
        email = email,
        dayOfBirth = dayOfBirth,
    )
}

// PUT
data class UpdateUserCommand(
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val dayOfBirth: LocalDate
)

// Mapping từ Command -> Entity của PUT
// map vào existing entity
fun UpdateUserCommand.updateEntity(existingUser: User) {
    existingUser.firstName = firstName
    existingUser.lastName = lastName
    existingUser.username = username
    existingUser.email = email
    existingUser.dayOfBirth = dayOfBirth
}