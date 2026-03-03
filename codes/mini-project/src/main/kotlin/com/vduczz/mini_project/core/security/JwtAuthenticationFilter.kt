package com.vduczz.mini_project.core.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter()
// extends OncePerRequestFilter
// -> lọc 1 lần cho mỗi request
{

    // filter-internal
    // OncePerRequestFilter -> đảm bảo trigger 1 lần cho mỗi request (HTTP Dispatch)
    override fun doFilterInternal(
        request: HttpServletRequest, // request
        response: HttpServletResponse, // response
        filterChain: FilterChain // filter chain
    ) {

        // trích xuất Authorization từ req's header
        val authHeader = request.getHeader("Authorization")

        // nếu thiếu thông tin xác thực
        // hoặc thông tin xác thực không phải của jwt (starts with: "Bearer ...")
        // => early return: bỏ qua jwt filter, đẩy lên chain tiếp theo
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        // ngược lại, nếu thông tin xác thực là của jwt

        // extract token from header
        val jwtToken = authHeader.substring(7)

        // ------------------------------------------------------------
        // extract & validate token
        val username = jwtService.extractUsername(jwtToken)

        // Authentication check
        //  + username != null -> token có chứa định danh
        //  + SecurityContextHolder.getContext().authentication == null
        //      -> request chưa được xác thực trong lifecycle hiện tại
        //      -> tức là mỗi HTTP request thuường chạy trên thread riêng
        //          + SecurityContextHolder: giữ security-context của thread hiện tại
        //          + authentication: chứa thông tin đăng nhập
        //              => authentication == null -> chưa có thông tin đăng nhập
        // => trường hợp đã validate nhưng trong Security Context của thread hiện tại chưa có
        if (SecurityContextHolder.getContext().authentication == null) {

            // load DB user theo username qua UserDetailsService
            val userDetails = userDetailsService.loadUserByUsername(username)

            // validate
            if (jwtService.isValidToken(jwtToken, userDetails)) {
                // khởi tạo lại UsernameAndPasswordAuthenticationToken
                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails, // object -> validated-user

                    null, // credentials == null vì dùng jwt nên không cần lưu password trong context

                    userDetails.authorities // user's roles
                    // phục vụ AuthorizationFilter, ...
                )

                // thêm thông tin Authentication Details: IP Address, session ID, .. của request
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)

                // add to SecurityContext
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        // jwt authenticate passed
        // -> chuyển request tới filter-chain tiếp theo
        filterChain.doFilter(request, response)
    }

}