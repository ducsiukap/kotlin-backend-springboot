package com.vduczz.mini_project.controller

import com.vduczz.mini_project.dto.request.CreateUserRequest
import com.vduczz.mini_project.dto.request.LoginRequest
import com.vduczz.mini_project.dto.request.toCommand
import com.vduczz.mini_project.dto.response.AuthResponse
import com.vduczz.mini_project.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth Controller")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {

    @Operation(summary = "Create a new user")
    @PostMapping("/register")
    fun register(
        @Valid
        @RequestBody
        request: CreateUserRequest,
    ): ResponseEntity<AuthResponse> {
        val response = authService.register(request.toCommand())
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Log in")
    @PostMapping("/login")
    fun login(
        @Valid
        @RequestBody
        request: LoginRequest,
    ): ResponseEntity<AuthResponse> {
        val response = authService.login(request.toCommand())
        return ResponseEntity.ok(response)
    }
}