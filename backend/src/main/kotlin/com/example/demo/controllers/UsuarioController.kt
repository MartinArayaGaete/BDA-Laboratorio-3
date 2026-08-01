package com.example.demo.controllers

import com.example.demo.dtos.ApiResponse
import com.example.demo.dtos.RegisterRequest
import com.example.demo.services.UsuarioService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = ["*"])
class UsuarioController(
    private val usuarioService: UsuarioService
) {
    private val logger = LoggerFactory.getLogger(UsuarioController::class.java)

    @GetMapping
    fun getAll(): ResponseEntity<ApiResponse> {
        val usuarios = usuarioService.findAll()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Total de usuarios: ${usuarios.size}",
                data = usuarios.map { it.toMap() }
            )
        )
    }

    @GetMapping("/{rut}")
    fun getByRut(@PathVariable rut: String): ResponseEntity<ApiResponse> {
        return usuarioService.findByRut(rut)
            .map { usuario ->
                ResponseEntity.ok(
                    ApiResponse(
                        success = true,
                        message = "Usuario encontrado",
                        data = usuario.toMap()
                    )
                )
            }
            .orElse(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse(false, "Usuario no encontrado")
                )
            )
    }

    @PostMapping
    fun create(@RequestBody request: RegisterRequest): ResponseEntity<ApiResponse> {
        logger.info("Creando usuario: ${request.rut}")

        return try {
            if (request.rut.isBlank() || request.nombre.isBlank() ||
                request.correo.isBlank() || request.contrasena.isBlank()
            ) {
                return ResponseEntity.badRequest().body(
                    ApiResponse(false, "Todos los campos son obligatorios")
                )
            }

            if (request.contrasena.length < 4) {
                return ResponseEntity.badRequest().body(
                    ApiResponse(false, "La contrasena debe tener al menos 4 caracteres")
                )
            }

            val usuario = usuarioService.registerUser(
                rut = request.rut,
                nombre = request.nombre,
                correo = request.correo,
                contrasena = request.contrasena,
                rol = request.rol
            )

            ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(
                    success = true,
                    message = "Usuario creado exitosamente",
                    data = usuario.toMap()
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ApiResponse(false, e.message ?: "Error de validacion")
            )
        } catch (e: Exception) {
            logger.error("Error al crear usuario", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(false, "Error interno del servidor")
            )
        }
    }
}

private fun com.example.demo.models.Usuario.toMap() = mapOf(
    "id" to id,
    "rut" to rut,
    "nombre" to nombre,
    "correo" to correo,
    "rol" to rol,
    "fecha_registro" to fechaRegistro.toString()
)