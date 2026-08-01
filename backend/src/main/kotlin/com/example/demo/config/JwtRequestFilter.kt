package com.example.demo.config

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtRequestFilter(
    private val jwtUtils: JwtUtils,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var token: String? = null

        // Intentar obtener de Cookie
        request.cookies?.forEach { cookie ->
            if (cookie.name == "token_acceso") {
                token = cookie.value
            }
        }

        // Si no hay cookie, buscar en Header Authorization
        if (token == null) {
            val authHeader = request.getHeader("Authorization")
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7)
            }
        }

        // Procesar token
        if (token != null) {
            try {
                val rut = jwtUtils.extractRut(token)
                val rol = jwtUtils.extractRol(token)

                if (rut != null && SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(rut)

                    if (jwtUtils.validateToken(token)) {
                        val authority = SimpleGrantedAuthority("ROLE_$rol")
                        val authentication = UsernamePasswordAuthenticationToken(
                            userDetails, null, listOf(authority)
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            } catch (e: ExpiredJwtException) {
                logger.warn("JWT expirado")
            } catch (e: Exception) {
                logger.error("Error al procesar JWT")
            }
        }

        filterChain.doFilter(request, response)
    }
}