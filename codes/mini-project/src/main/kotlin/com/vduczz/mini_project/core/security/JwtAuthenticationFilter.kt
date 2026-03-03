package com.vduczz.mini_project.core.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
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

    // bỏ qua filter kể cả có gửi kèm token
    // => bảo vệ Public API
    // cụ thể: do authen filter này chạy trước authorize trong Security config
    //      + kể cả authorize có .permitAll() thì jwt filter luôn chạy trước
    //          -> nếu gửi kèm token -> vẫn check và reject
    //      => để bảo vệ PUBLIC API (thực sự không cần authentication),
    //      => cần loại bỏ nó khỏi filter này
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath

        return path.startsWith("/api/v1/auth")
    }

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
        try {
            // try catch để bắt lỗi JwtException (ExpiredJwtException, SignatureException, MalformedJwtException...)
            // -> trả về 401/403 thay vì trả 500 (nếu không try-catch)

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
        } catch (e: Exception) {
            // jwt exception
            // -> có thể:
            //  + do nothing -> đẩy tới filter tiếp theo
            //      => ở filter tiêp theo, Spring Security thấy thiếu context
            //          -> tự động trả 401/403...
            //            println("Exception occurred: ${e.message}")

            //  + trả response ngay + return, không cho chạy tới filter tếp theo
            var statusCode = HttpServletResponse.SC_UNAUTHORIZED
            var errorMessage = e.message ?: "Unknown error"

            when (e) {
                is ExpiredJwtException -> errorMessage = "JWT Token expired"
                is SignatureException -> errorMessage = "JWT Token Invalid"
                is MalformedJwtException -> errorMessage = "Bad JWT Token"
                else -> statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            }

            // send error msg
            sendJwtFailedResponse(
                response = response,
                statusCode = statusCode,
                errorMessage = errorMessage,
            )

            // early return
            return
        }

        // jwt authenticate passed / exception
        // -> chuyển request tới filter-chain tiếp theo
        //      + trường hợp xảy ra exception và catch do nothing
        //          => ở filter tiêp theo, Spring Security thấy thiếu context
        //          -> tự động trả 401/403...
        //      + ở trường hợp catch + response ngay
        //          => đưa dòng bên dưới vào try cho chuẩn
        filterChain.doFilter(request, response)
    }

    private fun sendJwtFailedResponse(response: HttpServletResponse, statusCode: Int, errorMessage: String) {
        //
        response.status = statusCode

        // ép trả json
        response.contentType = "application/json; charset=utf-8"

        val errorDetails = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to statusCode,
            "error" to "Unauthorized",
            "message" to errorMessage
        )

        val mapper = ObjectMapper()
        response.writer.write(mapper.writeValueAsString(errorDetails))
    }

}