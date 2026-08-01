package com.example.demo.services

import com.example.demo.models.Usuario
import com.example.demo.repositories.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository
) {
    private val logger = LoggerFactory.getLogger(UsuarioService::class.java)

    fun findAll(): List<Usuario> {
        logger.debug("Buscando todos los usuarios")
        return usuarioRepository.findAll()
    }

    fun findByRut(rut: String): Optional<Usuario> {
        val rutLimpio = limpiarRut(rut)
        logger.debug("Buscando usuario con RUT: $rutLimpio")
        return usuarioRepository.findByRut(rutLimpio)
    }

    fun registerUser(
        rut: String,
        nombre: String,
        correo: String,
        contrasena: String,
        rol: String = "ARQUERO"
    ): Usuario {
        val rutLimpio = limpiarRut(rut)

        logger.info("Registrando nuevo usuario: $rutLimpio")

        if (usuarioRepository.existsByRut(rutLimpio)) {
            throw IllegalArgumentException("El RUT $rutLimpio ya esta registrado")
        }

        if (usuarioRepository.existsByCorreo(correo)) {
            throw IllegalArgumentException("El correo $correo ya esta registrado")
        }

        val rolValido = when (rol.uppercase()) {
            "ADMIN", "ARQUERO" -> rol.uppercase()
            else -> throw IllegalArgumentException("Rol no valido: $rol. Debe ser ADMIN o ARQUERO")
        }

        val usuario = Usuario(
            rut = rutLimpio,
            nombre = nombre,
            correo = correo,
            contrasena = contrasena,
            rol = rolValido
        )

        val saved = usuarioRepository.save(usuario)
        logger.info("Usuario creado exitosamente: ${saved.id}")
        return saved
    }

    fun validarLogin(rut: String, password: String): Optional<Usuario> {
        val rutLimpio = limpiarRut(rut)
        logger.info("Intento de login para RUT: $rutLimpio")

        val usuario = usuarioRepository.findByRut(rutLimpio)

        return if (usuario.isPresent && usuario.get().contrasena == password) {
            logger.info("Login exitoso para: $rutLimpio")
            usuario
        } else {
            logger.warn("Login fallido para: $rutLimpio")
            Optional.empty()
        }
    }

    private fun limpiarRut(rut: String): String {
        return rut.replace(".", "")
            .replace("-", "")
            .replace(",", "")
            .trim()
            .uppercase()
    }
}