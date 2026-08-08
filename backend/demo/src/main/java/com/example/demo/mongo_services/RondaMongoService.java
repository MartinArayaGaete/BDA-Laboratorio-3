package com.example.demo.mongo_services;

import com.example.demo.mongo_models.RondaDocument;
import com.example.demo.mongo_repositories.RondaMongoRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RondaMongoService {

    private final RondaMongoRepository repository;

    public RondaMongoService(RondaMongoRepository repository) {
        this.repository = repository;
    }

    public RondaDocument crear(String torneoId, int numeroRonda) {
        if (repository.existsByTorneoIdAndNumeroRonda(torneoId, numeroRonda)) {
            throw new IllegalArgumentException("Ya existe esa ronda en el torneo");
        }
        RondaDocument ronda = new RondaDocument();
        ronda.setTorneoId(torneoId);
        ronda.setNumeroRonda(numeroRonda);
        return repository.save(ronda);
    }

    public List<RondaDocument> findByTorneo(String torneoId) {
        return repository.findByTorneoIdOrderByNumeroRondaAsc(torneoId);
    }

    public RondaDocument findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ronda no encontrada"));
    }

    public RondaDocument iniciar(String id) {
        RondaDocument ronda = findById(id);
        ronda.setEstado("IN_COURSE");
        ronda.setFechaInicio(LocalDateTime.now());
        return repository.save(ronda);
    }

    public RondaDocument finalizar(String id) {
        RondaDocument ronda = findById(id);
        ronda.setEstado("FINISHED");
        return repository.save(ronda);
    }

    public void deleteById(String id) {
        RondaDocument ronda = findById(id);
        if (!"PENDIENTE".equals(ronda.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden eliminar rondas PENDIENTES");
        }
        repository.deleteById(id);
    }
}