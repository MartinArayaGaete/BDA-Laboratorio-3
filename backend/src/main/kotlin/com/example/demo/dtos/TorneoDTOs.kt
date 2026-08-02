package com.example.demo.dtos

import java.time.LocalDate

data class CrearTorneoRequest(
    val nombre: String,
    val fechaInicio: LocalDate,
    val fechaTermino: LocalDate,
    val plazasMax: Int,
    val categoriaNombre: String? = null,
    val distanciaTiro: Int? = null
)

data class TorneoResponse(
    val id: String?,
    val nombre: String,
    val estado: String,
    val fechaInicio: LocalDate,
    val fechaTermino: LocalDate,
    val plazasMax: Int,
    val plazasActual: Int,
    val plazasDisponibles: Int,
    val categoria: String
)