package com.example.demo.repositories

import com.example.demo.models.Usuario
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UsuarioRepository : MongoRepository<Usuario, String> {
    fun findByRut(rut: String): Optional<Usuario>
    fun findByCorreo(correo: String): Optional<Usuario>
    fun existsByRut(rut: String): Boolean
    fun existsByCorreo(correo: String): Boolean
    fun findByRol(rol: String): List<Usuario>
}