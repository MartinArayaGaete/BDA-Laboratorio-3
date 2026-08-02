package com.example.demo.mongo_services;

import com.example.demo.mongo_models.TorneoDocument;
import com.example.demo.mongo_dtos.TorneoMongoDTO;
import com.example.demo.mongo_mappers.TorneoMongoMapper;
import com.example.demo.mongo_repositories.TorneoMongoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TorneoMongoService {

    private final TorneoMongoRepository repository;

    public TorneoMongoService(TorneoMongoRepository repository) {
        this.repository = repository;
    }

    public TorneoDocument crear(TorneoMongoDTO dto) {
        if (repository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un torneo con ese nombre");
        }
        return repository.save(TorneoMongoMapper.toDocument(dto));
    }

    public List<TorneoDocument> findAll() {
        return repository.findAll();
    }

    public TorneoDocument findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));
    }

    public List<TorneoDocument> findByEstado(String estado) {
        return repository.findByEstado(estado);
    }
}