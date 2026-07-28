package com.thanh0x.coursedeal.security

import com.thanh0x.coursedeal.model.user.UserEntity
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class TokenProvider {

    @Value("\${custom.jwt-secret}")
    private var jwtSecret: String? = null

    @Value("\${custom.jwt-expiration:3600000}")
    private var jwtExpirationInMs: Long = 0

    private lateinit var key: Key

    @PostConstruct
    fun init() {
        key = if (jwtSecret == null || jwtSecret!!.length < 32) {
            Keys.secretKeyFor(SignatureAlgorithm.HS512)
        } else {
            Keys.hmacShaKeyFor(jwtSecret!!.toByteArray())
        }
    }

    fun createToken(user: UserEntity): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)

        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("email", user.email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getUserIdFromToken(token: String): Int? {
        val subject = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
        return subject.toIntOrNull()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            true
        } catch (ex: Exception) {
            false
        }
    }
}
