package com.example.demo.mongo_controllers;

import com.example.demo.mongo_services.ArqueroMongoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/mongo/arqueros")
@CrossOrigin(origins = "*")
public class ArqueroMongoController {

    private final ArqueroMongoService service;

    public ArqueroMongoController(ArqueroMongoService service) {
        this.service = service;
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<Map<String, Object>> obtenerHistorial(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(service.obtenerHistorial(id, page, size));
    }

    @GetMapping("/{id}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerEstadisticas(id));
    }
}