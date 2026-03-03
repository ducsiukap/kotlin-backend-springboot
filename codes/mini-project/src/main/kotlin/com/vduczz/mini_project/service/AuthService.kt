package com.vduczz.mini_project.service

import com.vduczz.mini_project.core.command.CreateUserCommand
import com.vduczz.mini_project.core.command.LoginCommand
import com.vduczz.mini_project.dto.response.AuthResponse

interface AuthService {

    fun register(command: CreateUserCommand): AuthResponse

    fun login(credential: LoginCommand): AuthResponse
}