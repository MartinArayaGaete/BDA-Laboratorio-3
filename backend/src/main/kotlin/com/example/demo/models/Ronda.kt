package com.example.demo.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "rondas")
data class Ronda(
    @Id
    val id: String? = null,
    val torneoId: String,
    val numeroRonda: Int,
    val estado: String = "PENDIENTE"
)