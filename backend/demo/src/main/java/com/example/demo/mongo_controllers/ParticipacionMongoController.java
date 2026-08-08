package com.example.demo.mongo_controllers;

import com.example.demo.mongo_models.ParticipacionDocument;
import com.example.demo.mongo_services.ParticipacionMongoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mongo/participaciones")
@CrossOrigin(origins = "*")
public class ParticipacionMongoController {

    private final ParticipacionMongoService service;

    public ParticipacionMongoController(ParticipacionMongoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ParticipacionDocument>> getAll() {
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

    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<ParticipacionDocument>> getByTorneo(@PathVariable String torneoId) {
        return ResponseEntity.ok(service.findByTorneo(torneoId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ParticipacionDocument>> getByUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.findByUsuario(usuarioId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            String torneoId = (String) body.get("torneoId");
            Long usuarioId = ((Number) body.get("usuarioId")).longValue();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(service.inscribir(torneoId, usuarioId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{torneoId}/{usuarioId}")
    public ResponseEntity<?> delete(@PathVariable String torneoId, @PathVariable Long usuarioId) {
        try {
            service.desinscribir(torneoId, usuarioId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}