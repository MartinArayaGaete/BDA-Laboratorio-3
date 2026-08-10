package com.example.demo.mongo_controllers;

import com.example.demo.mongo_models.ParticipacionDocument;
import com.example.demo.mongo_models.TorneoDocument;
import com.example.demo.mongo_dtos.TorneoMongoDTO;
import com.example.demo.mongo_repositories.ParticipacionMongoRepository;
import com.example.demo.mongo_repositories.PuntuacionMongoRepository;
import com.example.demo.mongo_services.GeospatialMongoService;
import com.example.demo.mongo_services.RondaMongoService;
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
    private final ParticipacionMongoRepository participacionMongoRepo;
    private final PuntuacionMongoRepository puntuacionMongoRepo;
    private final RondaMongoService rondaMongoService;
    private final GeospatialMongoService geospatialMongoService;

    public TorneoMongoController(TorneoMongoService service,
                                 ParticipacionMongoRepository participacionMongoRepo,
                                 PuntuacionMongoRepository puntuacionMongoRepo,
                                 RondaMongoService rondaMongoService,
                                 GeospatialMongoService geospatialMongoService) {
        this.service = service;
        this.participacionMongoRepo = participacionMongoRepo;
        this.puntuacionMongoRepo = puntuacionMongoRepo;
        this.rondaMongoService = rondaMongoService;
        this.geospatialMongoService = geospatialMongoService;
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

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarTorneo(@PathVariable String id, @RequestBody TorneoMongoDTO dto) {
        try {
            return ResponseEntity.ok(service.actualizarTorneo(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarTorneo(@PathVariable String id) {
        try {
            service.eliminarTorneo(id);
            return ResponseEntity.ok(Map.of("message", "Torneo eliminado exitosamente"));
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

    @PutMapping("/{id}/siguiente-ronda")
    public ResponseEntity<?> siguienteRonda(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.siguienteRonda(id));
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

    @GetMapping("/{id}/podio")
    public ResponseEntity<?> obtenerPodio(@PathVariable String id) {
        try {
            List<ParticipacionDocument> podio = participacionMongoRepo
                    .findByTorneoIdOrderByPosicionFinalAsc(id).stream()
                    .filter(p -> p.getPosicionFinal() != null && p.getPosicionFinal() <= 3)
                    .limit(3).toList();
            return ResponseEntity.ok(podio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/climas")
    public ResponseEntity<?> obtenerClimas(@PathVariable String id) {
        try {
            var torneo = service.findById(id);
            if (torneo.getZonaCompetenciaGeoJSON() == null) return ResponseEntity.ok(List.of());
            return ResponseEntity.ok(geospatialMongoService.obtenerCategoriasPorPoligono(torneo.getZonaCompetenciaGeoJSON()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/rondas/{numeroRonda}")
    public ResponseEntity<?> crearRonda(@PathVariable String id, @PathVariable int numeroRonda) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(rondaMongoService.crear(id, numeroRonda));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/rondas/{numeroRonda}/posiciones")
    public ResponseEntity<?> obtenerPosicionesRonda(@PathVariable String id, @PathVariable int numeroRonda) {
        try {
            return ResponseEntity.ok(service.obtenerPosicionesRonda(id, numeroRonda));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/rondas/{numeroRonda}/arqueros/{usuarioId}/posicion")
    public ResponseEntity<?> obtenerPosicionArquero(@PathVariable String id,
                                                    @PathVariable int numeroRonda,
                                                    @PathVariable Long usuarioId) {
        try {
            return ResponseEntity.ok(service.obtenerPosicionArqueroEnRonda(id, numeroRonda, usuarioId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}