package com.vduczz.mini_project.dto.request

import com.vduczz.mini_project.core.command.LoginCommand
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @NotBlank(message = "Login credentials is required!")
    val username: String,
    @NotBlank(message = "Login credentials is required!")
    val password: String
)

// LoginCommand at: core/command/LoginCommand
fun LoginRequest.toCommand(): LoginCommand = LoginCommand(
    username = this.username,
    password = this.password
)