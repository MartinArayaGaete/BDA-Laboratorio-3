package com.example.demo.controllers

import com.example.demo.dtos.LoginRequest
import com.example.demo.dtos.LoginResponse
import com.example.demo.services.UsuarioService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val usuarioService: UsuarioService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        logger.info("Solicitud de login recibida")

        if (request.rut.isBlank() || request.password.isBlank()) {
            return ResponseEntity.badRequest().body(
                LoginResponse(false, "RUT y contrasena son requeridos")
            )
        }

        val usuarioOpt = usuarioService.validarLogin(request.rut, request.password)

        return if (usuarioOpt.isPresent) {
            val u = usuarioOpt.get()
            ResponseEntity.ok(
                LoginResponse(
                    success = true,
                    message = "Login exitoso",
                    usuario = mapOf(
                        "id" to u.id,
                        "rut" to u.rut,
                        "nombre" to u.nombre,
                        "correo" to u.correo,
                        "rol" to u.rol
                    )
                )
            )
        } else {
            ResponseEntity.status(401).body(
                LoginResponse(false, "Credenciales invalidas")
            )
        }
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "OK",
                "service" to "Archery API",
                "version" to "1.0.0",
                "timestamp" to java.time.LocalDateTime.now().toString()
            )
        )
    }
}