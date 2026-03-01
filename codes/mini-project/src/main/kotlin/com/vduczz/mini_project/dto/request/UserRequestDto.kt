package com.vduczz.mini_project.dto.request

import com.vduczz.mini_project.core.command.CreateUserCommand
import com.vduczz.mini_project.core.command.UpdateUserCommand
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import java.time.LocalDate

// ------------------------------------------------------------
// Request DTO
// use `data class` for DTO

// validation được gắn vào các dto classes

// POST
data class CreateUserRequest(
    // field validation
    // use @field: (for kotlin)
    @field:NotBlank(
        // @NotNull + @NotEmpty + no accepts blank string
        message = "Firstname is required",
    )
    @field:Size(
        min = 1, max = 150,
        message = "Firstname must have least 1 character and no more than 150 characters"
    )
    val firstName: String,

    @field:NotBlank(
        // @NotNull + @NotEmpty + no accepts blank string
        message = "Lastname is required",
    )
    @field:Size( // size-limit
        min = 1, max = 50,
        message = "Lastname must have least 1 character and no more than 150 characters"
    )
    val lastName: String,

    @field:NotBlank("Username is required")
    @field:Size(
        min = 6, max = 50,
        message = "Username must have least 6 character and no more than 50 characters"
    )
    val username: String,

    @field:NotBlank("Password is required")
    @field:Size(
        min = 8, max = 50,
        message = "Password must have least 8 character and no more than 50 characters"
    )
    val password: String,

    @field:NotBlank("Email is required")
    @field:Email(message = "Invalid email")
    val email: String,

    @field:NotNull(message = "dayOfBirth is required")
    @field:Past(message = "invalid dayOfBirth")
    var dayOfBirth: LocalDate
)

// Mapping từ DTO -> Command
// nên đặt ngay dưới DTO
// using extension function map từ dto -> commnad
fun CreateUserRequest.toCommand(): CreateUserCommand {
    return CreateUserCommand(
        firstName = firstName,
        lastName = lastName,
        username = username,
        password = password,
        email = email,
        dayOfBirth = dayOfBirth,
    )
}

// PUT
data class UpdateUserRequest(
    @field:NotBlank(
        message = "Firstname is required",
    )
    @field:Size(
        min = 1, max = 150,
        message = "Firstname must have least 1 character and no more than 150 characters"
    )
    val firstName: String,

    @field:NotBlank(
        message = "Lastname is required",
    )
    @field:Size(
        min = 1, max = 50,
        message = "Lastname must have least 1 character and no more than 150 characters"
    )
    val lastName: String,

    @field:NotBlank("Username is required")
    @field:Size(
        min = 6, max = 50,
        message = "Username must have least 6 character and no more than 50 characters"
    )
    val username: String,

    @field:NotBlank("Password is required")
    @field:Size(
        min = 8, max = 50,
        message = "Password must have least 8 character and no more than 50 characters"
    )
    val email: String,

    @field:NotNull(message = "dayOfBirth is required")
    @field:Past(message = "invalid dayOfBirth")
    var dayOfBirth: LocalDate
)

// toCommand
fun UpdateUserRequest.toCommand(): UpdateUserCommand {
    return UpdateUserCommand(
        firstName = firstName,
        lastName = lastName,
        username = username,
        email = email,
        dayOfBirth = dayOfBirth
    )
}


// Patch nên dùng Map thay vì DTO vì nullable
