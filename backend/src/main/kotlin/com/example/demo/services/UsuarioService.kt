package com.example.demo.services

import com.example.demo.models.Usuario
import com.example.demo.repositories.UsuarioRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun findAll(): List<Usuario> = usuarioRepository.findAll()

    fun findByRut(rut: String): Optional<Usuario> = usuarioRepository.findByRut(rut)

    fun registerUser(rut: String, nombre: String, correo: String, contrasena: String, rol: String): Usuario {
        if (usuarioRepository.existsByRut(rut)) {
            throw IllegalArgumentException("El RUT ya está registrado")
        }
        val usuario = Usuario(
            rut = rut,
            nombre = nombre,
            correo = correo,
            contrasena = passwordEncoder.encode(contrasena) ?: contrasena,
            rol = rol
        )
        return usuarioRepository.save(usuario)
    }

    fun validarLogin(rut: String, rawPassword: String): Optional<Usuario> {
        val rutLimpio = rut.replace(".", "").replace(",", "").trim().uppercase()
        val usuario = usuarioRepository.findByRut(rutLimpio)

        return if (usuario.isPresent && passwordEncoder.matches(rawPassword, usuario.get().contrasena ?: "")) {
            usuario
        } else {
            Optional.empty()
        }
    }
}