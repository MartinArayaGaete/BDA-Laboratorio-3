package com.example.demo.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtils(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(rut: String, rol: String): String {
        return Jwts.builder()
            .subject(rut)
            .claim("rol", rol)
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(secretKey)
            .compact()
    }

    fun extractRut(token: String): String {
        return extractAllClaims(token).subject
    }

    fun extractRol(token: String): String {
        return extractAllClaims(token)["rol", String::class.java] ?: "ARQUERO"
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: JwtException) {
            false
        }
    }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}