package com.example.demo.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "torneos")
data class Torneo(
    @Id
    val id: String? = null,

    @Indexed(unique = true)
    val nombre: String,

    val estado: String = "PENDIENTE",

    val fechaInicio: LocalDate,
    val fechaTermino: LocalDate,

    val plazasMax: Int,
    val plazasActual: Int = 0,

    val categoria: Categoria = Categoria(),

    val participantesIds: List<String> = emptyList()
)

data class Categoria(
    val nombre: String = "RECURVO",
    val distanciaTiro: Int = 18
)