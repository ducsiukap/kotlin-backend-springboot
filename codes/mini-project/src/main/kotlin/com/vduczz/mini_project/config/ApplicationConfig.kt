package com.vduczz.mini_project.config

import com.vduczz.mini_project.core.exception.UserNotFoundException
import com.vduczz.mini_project.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class ApplicationConfig(
    private val userRepo: UserRepository,
) {

    // ============================================================
    // Bản chất UserDetailsService hoạt động như Adapter giúp Spring Security giao tiếp với DB
    // Khi có request, Spring Security sẽ:
    //      + lấy credentials -> lấy username (hoặc email, ...  tùy theo config)
    //      + gọi userDetailService và lấy entity theo username, bọc vào UserDetails
    //      + đối chiếu với pw
    @Bean
    fun userDetailsService(): UserDetailsService {
        // userDetailsService yêu cầu trả về 1 method:
        //  + nhận vào username (hoặc credentials khác như email, ..)
        //  + trả về entity
        return UserDetailsService { username ->
            // lấy entity trong db theo input
            userRepo.findByUsername(username)
            //          không thấy -> trả exception
                ?: throw UserNotFoundException(field = "username", value = username)


        }
    }

    // ============================================================
    // utility bean cho Security

    // ------------------------------------------------------------
    // password encoder (required)
    @Bean
    // sử dụng password encoder
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    // ------------------------------------------------------------
    // AuthenticationProvider
    @Bean
    fun authenticationProvider(): AuthenticationProvider {

        //  + sử dụng DaoAuthenticationProvider() chuẩn của Spring
        val authProvider = DaoAuthenticationProvider(
            userDetailsService() // -> truyền vào UserDetailsService
        )

        //  + password encoder -> required
        authProvider.setPasswordEncoder(passwordEncoder())

        return authProvider
    }

    // ------------------------------------------------------------
    // AuthenticationManager -> quản lí authentication
    //  + sử dụng ở controller/service
    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}