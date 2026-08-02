package com.example.demo.controllers

import com.example.demo.dtos.CrearTorneoRequest
import com.example.demo.dtos.TorneoResponse
import com.example.demo.services.TorneoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/torneos")
@CrossOrigin(origins = ["*"])
class TorneoController(
    private val torneoService: TorneoService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<TorneoResponse>> =
        ResponseEntity.ok(torneoService.findAll())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<TorneoResponse> =
        torneoService.findById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @GetMapping("/estado/{estado}")
    fun getByEstado(@PathVariable estado: String): ResponseEntity<List<TorneoResponse>> =
        ResponseEntity.ok(torneoService.findByEstado(estado))

    @PostMapping
    fun create(@RequestBody request: CrearTorneoRequest): ResponseEntity<Any> {
        return try {
            val response = torneoService.crear(request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}/estado")
    fun cambiarEstado(
        @PathVariable id: String,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Any> {
        return try {
            val estado = body["estado"] ?: throw IllegalArgumentException("Campo 'estado' requerido")
            ResponseEntity.ok(torneoService.cambiarEstado(id, estado))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            torneoService.deleteById(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}