package com.example.demo.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "usuarios")
data class Usuario(
    @Id
    val id: String? = null,

    @Indexed(unique = true)
    val rut: String,
    val nombre: String,

    @Indexed(unique = true)
    val correo: String,
    val contrasena: String,
    val rol: String = "ARQUERO"  // ADMIN, ARQUERO
)