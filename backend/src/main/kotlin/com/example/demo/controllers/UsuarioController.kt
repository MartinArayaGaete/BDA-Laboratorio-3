package com.example.demo.controllers

import com.example.demo.services.UsuarioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class RegisterRequest(
    val rut: String,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: String = "ARQUERO"
)

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = ["*"])
class UsuarioController(
    private val usuarioService: UsuarioService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<*>> {
        val usuarios = usuarioService.findAll()
        return if (usuarios.isEmpty()) ResponseEntity.noContent().build()
        else ResponseEntity.ok(usuarios.map { u ->
            mapOf("id" to u.id, "rut" to u.rut, "nombre" to u.nombre, "correo" to u.correo, "rol" to u.rol)
        })
    }

    @PostMapping
    fun create(@RequestBody request: RegisterRequest): ResponseEntity<*> {
        return try {
            val usuario = usuarioService.registerUser(
                request.rut, request.nombre, request.correo, request.contrasena, request.rol
            )
            ResponseEntity.status(HttpStatus.CREATED).body(
                mapOf("id" to usuario.id, "rut" to usuario.rut, "nombre" to usuario.nombre, "rol" to usuario.rol)
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}