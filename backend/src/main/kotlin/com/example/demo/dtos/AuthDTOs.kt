package com.example.demo.dtos

data class LoginRequest(
    val rut: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val usuario: Map<String, Any?>? = null
)