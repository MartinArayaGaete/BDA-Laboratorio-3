package com.example.demo.mongo_services;

import com.example.demo.mongo_models.PuntuacionDocument;
import com.example.demo.mongo_repositories.PuntuacionMongoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PuntuacionMongoService {

    private final PuntuacionMongoRepository repository;

    public PuntuacionMongoService(PuntuacionMongoRepository repository) {
        this.repository = repository;
    }

    public void guardarOActualizar(PuntuacionDocument document) {
        PuntuacionDocument target = repository
                .findByTorneoIdAndRondaIdAndUsuarioId(document.getTorneoId(), document.getRondaId(), document.getUsuarioId())
                .orElseGet(PuntuacionDocument::new);

        if (target.getId() == null) {
            target.setCreatedAt(LocalDateTime.now());
        }

        target.setTorneoId(document.getTorneoId());
        target.setRondaId(document.getRondaId());
        target.setUsuarioId(document.getUsuarioId());
        target.setNombreArquero(document.getNombreArquero());
        target.setNombreTorneo(document.getNombreTorneo());
        target.setNumeroRonda(document.getNumeroRonda());
        target.setCategoria(document.getCategoria());
        target.setFlechas(document.getFlechas());
        target.setPuntajeTotal(document.getPuntajeTotal());
        target.setPosicionArquero(document.getPosicionArquero());
        target.setPosicionDiana(document.getPosicionDiana());
        target.setUpdatedAt(LocalDateTime.now());

        repository.save(target);
    }
}