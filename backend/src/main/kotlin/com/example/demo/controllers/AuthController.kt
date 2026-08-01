package com.example.demo.controllers

import com.example.demo.config.JwtUtils
import com.example.demo.services.UsuarioService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class LoginRequest(val rut: String, val password: String)
data class UserInfoResponse(val id: String, val rut: String, val rol: String, val correo: String, val nombre: String)

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val usuarioService: UsuarioService,
    private val jwtUtils: JwtUtils
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest, response: HttpServletResponse): ResponseEntity<*> {
        val usuarioOpt = usuarioService.validarLogin(request.rut, request.password)

        return if (usuarioOpt.isPresent) {
            val usuario = usuarioOpt.get()
            val token = jwtUtils.generateToken(usuario.rut, usuario.rol)

            val cookie = Cookie("token_acceso", token).apply {
                isHttpOnly = true
                secure = false
                path = "/"
                maxAge = 3600
            }
            response.addCookie(cookie)

            ResponseEntity.ok(
                UserInfoResponse(
                    id = usuario.id!!,
                    rut = usuario.rut,
                    rol = usuario.rol,
                    correo = usuario.correo,
                    nombre = usuario.nombre
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build<Any>()
        }
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<String> {
        val cookie = Cookie("token_acceso", null).apply {
            path = "/"
            isHttpOnly = true
            maxAge = 0
        }
        response.addCookie(cookie)
        return ResponseEntity.ok("Sesión cerrada")
    }

    @PostMapping("/refresh-token")
    fun refreshToken(
        @RequestHeader(value = "Authorization", required = false) authHeader: String?,
        @CookieValue(value = "token_acceso", required = false) cookieToken: String?,
        response: HttpServletResponse
    ): ResponseEntity<String> {
        val token = cookieToken ?: authHeader?.removePrefix("Bearer ")

        if (token == null || !jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val rut = jwtUtils.extractRut(token)
        val usuarioOpt = usuarioService.findByRut(rut)

        return if (usuarioOpt.isPresent) {
            val nuevoToken = jwtUtils.generateToken(usuarioOpt.get().rut, usuarioOpt.get().rol)

            val cookie = Cookie("token_acceso", nuevoToken).apply {
                isHttpOnly = true
                path = "/"
                maxAge = 3600
            }
            response.addCookie(cookie)

            ResponseEntity.ok(nuevoToken)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}