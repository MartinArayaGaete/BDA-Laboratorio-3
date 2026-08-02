package com.example.demo.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "puntuaciones")
data class Puntuacion(
    @Id
    val id: String? = null,
    val torneoId: String,
    val rondaId: String,
    val arqueroId: String,
    val nombreArquero: String,
    val flechas: List<Int> = emptyList(),
    val puntajeTotal: Int = 0
)