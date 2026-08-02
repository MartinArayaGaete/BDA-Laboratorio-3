package com.example.demo.mappers

import com.example.demo.dtos.TorneoResponse
import com.example.demo.models.Torneo

object TorneoMapper {

    fun toResponse(torneo: Torneo): TorneoResponse {
        return TorneoResponse(
            id = torneo.id,
            nombre = torneo.nombre,
            estado = torneo.estado,
            fechaInicio = torneo.fechaInicio,
            fechaTermino = torneo.fechaTermino,
            plazasMax = torneo.plazasMax,
            plazasActual = torneo.plazasActual,
            plazasDisponibles = torneo.plazasMax - torneo.plazasActual,
            categoria = torneo.categoria.nombre
        )
    }

    fun toResponseList(torneos: List<Torneo>): List<TorneoResponse> {
        return torneos.map { toResponse(it) }
    }
}