package com.example.demo.mongo_controllers;

import com.example.demo.mongo_services.PipelineEstadisticasService;
import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mongo/pipeline")
@CrossOrigin(origins = "*")
public class PipelineEstadisticasController {

    private final PipelineEstadisticasService service;

    public PipelineEstadisticasController(PipelineEstadisticasService service) {
        this.service = service;
    }

    @GetMapping("/rendimiento")
    public ResponseEntity<List<Document>> obtenerRendimiento() {
        return ResponseEntity.ok(service.ejecutarPipelineRendimiento());
    }
}