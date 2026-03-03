package com.vduczz.mini_project.core.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    // jwt props
    @Value("\${application.security.jwt.secret-key}")
    private var secretKey: String,

    @Value("\${application.security.jwt.expiration}")
    private var expiration: Long
) {

    // ------------------------------------------------------------
    // common util function
    // -> secret-key
    fun getSigningKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    // ============================================================
    // Serializaiton (Generate)
    fun generateToken(
        extraClaims: Map<String, Any> = emptyMap(),
        userDetails: UserDetails
    ): String {
        return Jwts.builder() // jwt builder
            .claims(extraClaims) // additional information
            .subject(userDetails.username) // subject -> always is identity (username / id / ... )
            .issuedAt(Date(System.currentTimeMillis())) // start date
            .expiration(Date(System.currentTimeMillis() + expiration)) // expiration date
            .signWith(getSigningKey()) // sign with SecretKey, default sign algorithm: HS256
            .compact() // build to string

        // jjwt version cũ ( < 0.12.x ) hơn dùng set... như setClaims().setSubject()...
    }

    // ============================================================
    // Deserialization
    // ------------------------------------------------------------
    // utils method
    private fun extractAllClaims(token: String): Claims {
        // extract all extraClaims in genarateToken()

        return Jwts.parser() // start deserialization
            .verifyWith(getSigningKey()) // verify token
            .build() //
            .parseSignedClaims(token)
            .payload

        // ở jjwt cũ:
        //  - verifyWith -> setSigningKey
        //  - parseSignedClaims -> parseClaimsJws
        //  - payload -> body
    }

    // ------------------------------------------------------------
    // extract each claim
    fun <T> extractClaim(token: String, claimResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimResolver(claims)
    }

    // extract subject
    fun extractUsername(token: String): String {
        return extractClaim(token) { it.subject }
    }

    // extract expiration
    fun extractExpiration(token: String): Date {
        return extractClaim(token) { it.expiration }
    }

    // ============================================================
    // ------------------------------------------------------------
    // utils method
    private fun isTokenNotExpired(token: String): Boolean {
        return extractExpiration(token).after(Date())
    }

    // ------------------------------------------------------------
    // Validate
    fun isValidToken(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)

        return (username == userDetails.username)
                && isTokenNotExpired(token)
    }

}