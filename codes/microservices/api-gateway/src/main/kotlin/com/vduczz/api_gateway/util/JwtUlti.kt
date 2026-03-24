package com.vduczz.api_gateway.util

import com.vduczz.api_gateway.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

// JwtUitl: JWT service to verify JWT & extract information
@Component
class JwtUlti(private val jwtProps: JwtProperties) {

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(
            jwtProps.secret.toByteArray(Charsets.UTF_8)
        )
    }

    /* ========================================
    * Deserialize token
    ======================================== */
    private fun extractAllClaims(token: String): Claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .payload

    private fun <T> extractClaim(token: String, resolver: (Claims) -> T): T = resolver(extractAllClaims(token))

    // -------------------------
    // VERIFY
    fun isValidToken(token: String): Boolean {
        return try {
            val expiration = extractClaim(token) { it.expiration }
            expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    // -------------------------
    // Extract claims
    fun extractUsername(token: String): String = extractClaim(token) { it.subject }
    fun extractUserId(token: String): String = extractClaim(token) { it["userId"] as? String ?: "" }
    fun extractRole(token: String): String = extractClaim(token) { it["role"] as? String ?: "" }
}

