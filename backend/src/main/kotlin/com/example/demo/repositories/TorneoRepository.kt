package com.example.demo.repositories

import com.example.demo.models.Torneo
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TorneoRepository : MongoRepository<Torneo, String> {
    fun findByEstado(estado: String): List<Torneo>
    fun countByEstado(estado: String): Long
    fun existsByNombre(nombre: String): Boolean
    fun findByPlazasActualLessThan(plazas: Int): List<Torneo>
}