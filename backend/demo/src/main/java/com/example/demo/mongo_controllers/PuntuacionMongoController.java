package com.example.demo.mongo_controllers;

import com.example.demo.mongo_models.PuntuacionDocument;
import com.example.demo.mongo_services.PuntuacionMongoService;
import com.example.demo.mongo_services.PuntuacionEstadisticaService;

import org.bson.Document;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mongo/puntuaciones")
@CrossOrigin(origins = "*")
public class PuntuacionMongoController {

    private final PuntuacionMongoService service;
    private final PuntuacionEstadisticaService estadisticaService;

    public PuntuacionMongoController(
            PuntuacionMongoService service,
            PuntuacionEstadisticaService estadisticaService) {

        this.service = service;
        this.estadisticaService = estadisticaService;
    }


    @GetMapping
    public ResponseEntity<List<PuntuacionDocument>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }


    @GetMapping("/estadisticas")
    public ResponseEntity<List<Document>> getEstadisticas() {
        return ResponseEntity.ok(
                estadisticaService.obtenerEstadisticas()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<PuntuacionDocument>> getByTorneo(
            @PathVariable String torneoId) {

        return ResponseEntity.ok(
                service.findByTorneo(torneoId)
        );
    }


    @GetMapping("/ronda/{rondaId}")
    public ResponseEntity<List<PuntuacionDocument>> getByRonda(
            @PathVariable String rondaId) {

        return ResponseEntity.ok(
                service.findByRonda(rondaId)
        );
    }


    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PuntuacionDocument>> getByUsuario(
            @PathVariable Long usuarioId) {

        return ResponseEntity.ok(
                service.findByUsuario(usuarioId)
        );
    }


    @GetMapping("/ranking/{torneoId}")
    public ResponseEntity<List<PuntuacionDocument>> getRanking(
            @PathVariable String torneoId) {

        return ResponseEntity.ok(
                service.getRanking(torneoId)
        );
    }


    @PostMapping
    public ResponseEntity<?> createOrUpdate(@RequestBody PuntuacionDocument doc) {
        try {
            PuntuacionDocument saved = service.guardarOActualizarConReglamento(doc);  // ← Cambiar aquí
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable String id) {

        try {

            service.deleteById(id);

            return ResponseEntity
                    .noContent()
                    .build();

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}