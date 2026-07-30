package com.thanh0x.coursedeal.security

import com.thanh0x.coursedeal.config.IdentityProperties
import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.model.user.UserEntity
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
@EnableConfigurationProperties(IdentityProperties::class)
class TokenProvider(private val properties: IdentityProperties) {
    private val log = logger()
    private lateinit var key: Key

    companion object {
        private const val MIN_SECRET_LENGTH = 32
    }

    @PostConstruct
    fun init() {
        key =
            if (properties.jwtSecret.length < MIN_SECRET_LENGTH) {
                Keys.secretKeyFor(SignatureAlgorithm.HS512)
            } else {
                Keys.hmacShaKeyFor(properties.jwtSecret.toByteArray())
            }
    }

    fun createToken(user: UserEntity): String {
        val now = Date()
        val expiryDate = Date(now.time + properties.jwtExpiration)

        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("email", user.email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getUserIdFromToken(token: String): Int? {
        val subject =
            Jwts.parserBuilder()
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
        } catch (ex: JwtException) {
            log.debug("Rejected invalid JWT: {}", ex.message)
            false
        } catch (ex: IllegalArgumentException) {
            log.debug("Rejected invalid JWT: {}", ex.message)
            false
        }
    }
}
