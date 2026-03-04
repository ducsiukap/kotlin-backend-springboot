package com.vduczz.mini_project.core.security

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.time.LocalDateTime
import java.util.function.Supplier

@Component
class RateLimitFilter(
    // proxy to redis
    private val proxyManager: ProxyManager<ByteArray>,
) : OncePerRequestFilter() {

    // bucket config -> rules
    private val bucketConfig = BucketConfiguration.builder()
        .addLimit(
            Bandwidth.builder()
                .capacity(5) // tối đa 5 request

                // refillGreedy -> 12s/lần hồi thay vì đợi 60s và hồi full 5 lượt request
                .refillGreedy(5, Duration.ofMinutes(1)) // hồi 5 request mỗi phút
                .build()
        ).build()

    // mục tiêu
    // shouldNotFilter :
    //  + true -> no-filter
    //  + false -> apply filter
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath

        // !path.startWith() -> chỉ limit /auth
        return !(path.startsWith("/api/v1/auth"))
    }


    // apply filter
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // ============================================================
        // client info => định danh client
        val ip = request.remoteAddr ?: "unknown_ip" // client ip
        val userAgent = request.getHeader("User-Agent") ?: "unknown_ua" // client browser
        // val deviceId = request.getHeader("Device-Id") ?: "unknown_device" // device id
        val clientFingerprint = "$ip-$userAgent"
        // Bucket4j Redis yêu cầu Key phải là mảng Byte
        val keyBytes = clientFingerprint.toByteArray()


        // ============================================================
        // nếu redis chết -> cho vượt qua filter + log
        try {

            // proxy
            val bucket = proxyManager.builder().build(keyBytes) { bucketConfig }
            val probe = bucket.tryConsumeAndReturnRemaining(1)

            // kiểm tra
            if (probe.isConsumed) { // còn lượt request -> pass

                // rate limit remaining header
                response.addHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())

                // chuyen len filter chain tiep theo
                filterChain.doFilter(request, response)

            } else { // hết lượt request

                val timeToRefill = probe.nanosToWaitForRefill / 1_000_000_000

                // ============================================================
                // response
                response.addHeader("Retry-After", timeToRefill.toString())
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.contentType = "application/json;charset=UTF-8"

                response.writer.write(
                    """
                        {
                            "timestamp": "${System.currentTimeMillis()}",
                            "status": ${HttpStatus.TOO_MANY_REQUESTS.value()},
                            "error": "${HttpStatus.TOO_MANY_REQUESTS.name}",
                            "message": "Rate limited! Try again after ${timeToRefill}s"
                        }
                    """.trimIndent()
                )
            }
        } catch (e: Exception) {
            println("[RateLimitFilter]  error: ${e.message}]")

            // cho client qua filter
            filterChain.doFilter(request, response)
        }

    }


}