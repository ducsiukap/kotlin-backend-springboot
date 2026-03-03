package com.vduczz.mini_project.config

import com.vduczz.mini_project.core.security.JwtAuthenticationFilter
import com.vduczz.mini_project.model.UserRoles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity // -> yêu cầu Spring Context bỏ qua auto-configuration
// và sử dụng class này cho config chính của Web Security
@EnableMethodSecurity
class SecurityConfig(

    // filters
    private val jwtAuthFilter: JwtAuthenticationFilter,

    // auth provider
    private val authProvider: AuthenticationProvider
) {

    @Bean
    // create Security Filter Chain
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // vô hiệu hóa CSRF - Cross-Site Request Forgery protection
            //  + csrf khắc phục lỗ hổng CSRF của session/cookie
            //  + jwt là stateless -> không bị lỗi này -> disable
            .csrf { it.disable() }

            // Authorization Rules cho từng endpoint
            .authorizeHttpRequests { auth ->
                // note: authorizeHttpRequests áp dụng rule: Top-Down Execution
                //  -> match cái nào trước dùng cái đó, bỏ qua các matcher bên dưới
                //      so the flow should be:
                //          + uri that can .permitAll() at the first
                //          + uri that requires full-auth (authen + author) at the second
                //          + uri requires authentication only at the last
                auth
                    // .requestMatchers -> match url
                    // .permitAll() at the first
                    .requestMatchers(
                        // Auth API
                        "/api/v1/auth/**",

                        // Swagger
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources",
                        "/swagger-resources/**",
                        "/configuration/ui",
                        "/configuration/security",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/swagger-ui.html",

                        // endpoint cho error
                        "/error"
                    ).permitAll() // .permitAll() bypass toàn bộ
                    // -> không yêu cầu auth (no-authen & no-author)


                    // ------------------------------------------------------------
                    // require both authentication & authorization
                    // .hasRole() / .hasAnyRole() at the second
                    // RBAC - role-based access control
                    // -> phân quyền truy cập dựa vào role
                    //      => yêu cầu chỉ có một or một vài role cụ thể được phép truy cập vào resources
                    //  + config in SecurityConfig
                    //      -> .requestMatchers().hasRole() / .requestMatchers().hasAnyRole()
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole(UserRoles.ADMIN.name) // only token has authorizes including "ROLE_ADMIN" can be accepted
                    // or .hasAnyRow(UserRoles.ADMIN.name, UserRoles.USER.name, ...) -> accessible for multiple roles

                    //  + or require role to access in controller
                    //      + use @EnableMethodSecurity at the top of this class (SecurityConfig)
                    //      + use @PreAuthorize("hasRole('ADMIN')") / @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
                    //          at the controller (top of class or top of function are ok)
                    // ============================================================
                    //  NÊN SỬ DỤNG @PreAuthorize Ở CONTROLLER HƠN!!!
                    //  NÊN SỬ DỤNG @PreAuthorize Ở CONTROLLER HƠN!!!
                    //  NÊN SỬ DỤNG @PreAuthorize Ở CONTROLLER HƠN!!!
                    // ============================================================

                    // ------------------------------------------------------------
                    // require authentication only
                    // .anyRequest().authenticated() at the last
                    // mọi request tới URI khác -> bắt buộc phải có SecurityContext
                    .anyRequest().authenticated() // only authen is required
            }

            // chuyển Session policy -> Stateless
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            // đăng kí auth provider
            .authenticationProvider(authProvider)

            // yêu cầu Jwt Authentication trước UsernameAndPassword
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

}