package com.example.demo.dtos

data class RegisterRequest(
    val rut: String,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: String = "ARQUERO"
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)