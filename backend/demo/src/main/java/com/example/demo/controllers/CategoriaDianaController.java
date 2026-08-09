package com.example.demo.controllers;

import com.example.demo.models.CategoriaDiana;
import com.example.demo.services.CategoriaDianaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias-diana")
@CrossOrigin(origins = "*")
public class CategoriaDianaController {

    private final CategoriaDianaService service;

    public CategoriaDianaController(CategoriaDianaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDiana>> getAll() {
        return ResponseEntity.ok(service.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDiana> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CategoriaDiana categoria) {
        service.crear(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body("Categoría diana creada");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody CategoriaDiana categoria) {
        service.actualizar(id, categoria);
        return ResponseEntity.ok("Categoría diana actualizada");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok("Categoría diana eliminada");
    }
}