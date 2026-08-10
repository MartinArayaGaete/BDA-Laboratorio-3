package com.example.demo.mongo_controllers;

import com.example.demo.mongo_models.RondaDocument;
import com.example.demo.mongo_services.RondaMongoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mongo/rondas")
@CrossOrigin(origins = "*")
public class RondaMongoController {

    private final RondaMongoService service;

    public RondaMongoController(RondaMongoService service) {
        this.service = service;
    }

    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<RondaDocument>> getByTorneo(@PathVariable String torneoId) {
        return ResponseEntity.ok(service.findByTorneo(torneoId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            String torneoId = (String) body.get("torneoId");
            int numeroRonda = (int) body.get("numeroRonda");
            return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(torneoId, numeroRonda));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/iniciar")
    public ResponseEntity<?> iniciar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.iniciar(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.finalizar(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/zona-ambiental")
    public ResponseEntity<?> asignarZonaAmbiental(@PathVariable String id, @RequestBody Map<String, Long> body) {
        try {
            Long idZonaAmbiental = body.get("idZonaAmbiental");
            return ResponseEntity.ok(service.asignarZonaAmbiental(id, idZonaAmbiental));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


}