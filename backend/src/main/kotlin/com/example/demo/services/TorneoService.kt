package com.example.demo.services

import com.example.demo.dtos.CrearTorneoRequest
import com.example.demo.dtos.TorneoResponse
import com.example.demo.mappers.TorneoMapper
import com.example.demo.models.Categoria
import com.example.demo.models.Torneo
import com.example.demo.repositories.TorneoRepository
import org.springframework.stereotype.Service

@Service
class TorneoService(
    private val torneoRepository: TorneoRepository
) {

    fun crear(request: CrearTorneoRequest): TorneoResponse {
        if (torneoRepository.existsByNombre(request.nombre)) {
            throw IllegalArgumentException("Ya existe un torneo con ese nombre")
        }
        if (request.plazasMax <= 0) {
            throw IllegalArgumentException("Las plazas deben ser mayores a 0")
        }

        val torneo = Torneo(
            nombre = request.nombre,
            fechaInicio = request.fechaInicio,
            fechaTermino = request.fechaTermino,
            plazasMax = request.plazasMax,
            categoria = Categoria(
                nombre = request.categoriaNombre ?: "RECURVO",
                distanciaTiro = request.distanciaTiro ?: 18
            )
        )

        return TorneoMapper.toResponse(torneoRepository.save(torneo))
    }

    fun findAll(): List<TorneoResponse> =
        TorneoMapper.toResponseList(torneoRepository.findAll())

    fun findById(id: String): TorneoResponse? {
        return torneoRepository.findById(id)
            .map { TorneoMapper.toResponse(it) }
            .orElse(null)
    }

    fun findByEstado(estado: String): List<TorneoResponse> =
        TorneoMapper.toResponseList(torneoRepository.findByEstado(estado))

    fun cambiarEstado(id: String, nuevoEstado: String): TorneoResponse {
        val estadosValidos = listOf("PENDIENTE", "IN_COURSE", "FINISHED")
        if (nuevoEstado !in estadosValidos) {
            throw IllegalArgumentException("Estado no valido. Usar: ${estadosValidos.joinToString()}")
        }

        val torneo = torneoRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Torneo no encontrado") }

        return TorneoMapper.toResponse(
            torneoRepository.save(torneo.copy(estado = nuevoEstado))
        )
    }

    fun deleteById(id: String) {
        if (!torneoRepository.existsById(id)) {
            throw IllegalArgumentException("Torneo no encontrado")
        }
        torneoRepository.deleteById(id)
    }
}