package com.example.demo.mongo_controllers;

import com.example.demo.mongo_models.TorneoDocument;
import com.example.demo.mongo_dtos.TorneoMongoDTO;
import com.example.demo.mongo_services.TorneoMongoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mongo/torneos")
@CrossOrigin(origins = "*")
public class TorneoMongoController {

    private final TorneoMongoService service;

    public TorneoMongoController(TorneoMongoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TorneoDocument>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TorneoMongoDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/iniciar")
    public ResponseEntity<?> iniciarTorneo(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.iniciarTorneo(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarTorneo(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.finalizarTorneo(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/zonas-ambientales/sincronizar")
    public ResponseEntity<?> sincronizarZonasAmbientales(@PathVariable String id, @RequestParam Long sqlIdTorneo) {
        try {
            return ResponseEntity.ok(service.sincronizarZonasAmbientalesDesdeSql(id, sqlIdTorneo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/{id}/siguiente-ronda")
    public ResponseEntity<?> siguienteRonda(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.siguienteRonda(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


}