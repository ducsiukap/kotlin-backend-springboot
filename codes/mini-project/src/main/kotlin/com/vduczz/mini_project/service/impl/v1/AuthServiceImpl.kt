package com.vduczz.mini_project.service.impl.v1

import com.vduczz.mini_project.core.command.CreateUserCommand
import com.vduczz.mini_project.core.command.LoginCommand
import com.vduczz.mini_project.core.command.toEntity
import com.vduczz.mini_project.core.exception.DuplicateUsernameException
import com.vduczz.mini_project.core.security.JwtService
import com.vduczz.mini_project.dto.response.AuthResponse
import com.vduczz.mini_project.repository.UserRepository
import com.vduczz.mini_project.service.AuthService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
    private val userRepo: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
) : AuthService {

    // ============================================================
    // register
    override fun register(command: CreateUserCommand): AuthResponse {
        // check duplicate username
        val userDB = userRepo.findByUsername(command.username)
        if (userDB != null) throw DuplicateUsernameException(username = command.username)

        // command -> dto
        val user = command.toEntity()
        user.password = passwordEncoder.encode(command.password)
            ?: throw IllegalArgumentException("Password encoding failed")

        // save to db
        val savedUser = userRepo.save(user)

        // take jwt-token
        val jwtToken = jwtService.generateToken(userDetails = savedUser)

        return AuthResponse(
            userId = savedUser.id!!,
            token = jwtToken,
        )
    }

    // ============================================================
    // login
    override fun login(credential: LoginCommand): AuthResponse {

        // Delegate Authentication process
        //  -> trigger DaoAuthenticationProvider
        //      => sai credentials -> throw BadCredentialsException
        // code flow:
        //  - AuthenticationManager trigger DaoAuthenticationProvider
        //  - then, DaoAuthenticationProvider invokes UserDetailsService to take user's data from db
        //  - encode password and compare
        //  -> return: pass (success) or throw exception (failed)
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(credential.username, credential.password),
        )

        // login successfully
        // -> take user's data (UserDetails)
        val user = userRepo.findByUsername(credential.username)
            ?: throw RuntimeException("User not found after authentication")
        // generate token to return to the client
        val jwtToken = jwtService.generateToken(userDetails = user)

        return AuthResponse(
            userId = user.id!!,
            token = jwtToken,
        )
    }

    // ============================================================
    // Logout
    //  >>> bản chất JWT: không thể thu hồi - revoke - khi đã được tạo (trừ expiration)
    // to handle client logout:
    //  + Client-side logout:
    //      >>> Logout do Frontend đảm nhiệm 100% -> xóa token sau khi User click logout
    //          => các request tiếp theo thiếu `jwt token` -> không có Header Authorization
    //  + Token Blacklisting (cho hệ thống cần bảo mật khắt khe)
    //      + create API: /logout
    //      + save blacklist: when user send request to /logout -> server save jwt-token to db (or Redis, ..)
    //          with TTL = remaining time of token
    //      >>> so, at JwtAuthenticationFilter, after verify signature,
    //          we have to check blacklist token:
    //              if (redisBlacklist.contains(jwtToken)) throw TokenExpiredException(...)
}