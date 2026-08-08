package com.example.demo.mongo_services;

import com.example.demo.mongo_models.PuntuacionDocument;
import com.example.demo.mongo_models.RondaDocument;
import com.example.demo.mongo_models.TorneoDocument;
import com.example.demo.mongo_dtos.TorneoMongoDTO;
import com.example.demo.mongo_mappers.TorneoMongoMapper;
import com.example.demo.mongo_repositories.TorneoMongoRepository;
import com.example.demo.mongo_repositories.RondaMongoRepository;
import com.example.demo.mongo_repositories.PuntuacionMongoRepository;
import com.example.demo.mongo_repositories.ParticipacionMongoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TorneoMongoService {

    private final TorneoMongoRepository torneoMongoRepo;
    private final RondaMongoRepository rondaMongoRepo;
    private final PuntuacionMongoRepository puntuacionMongoRepo;
    private final ParticipacionMongoRepository participacionMongoRepo;

    public TorneoMongoService(TorneoMongoRepository torneoMongoRepo,
                              RondaMongoRepository rondaMongoRepo,
                              PuntuacionMongoRepository puntuacionMongoRepo,
                              ParticipacionMongoRepository participacionMongoRepo) {
        this.torneoMongoRepo = torneoMongoRepo;
        this.rondaMongoRepo = rondaMongoRepo;
        this.puntuacionMongoRepo = puntuacionMongoRepo;
        this.participacionMongoRepo = participacionMongoRepo;
    }

    public TorneoDocument crear(TorneoMongoDTO dto) {
        if (torneoMongoRepo.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un torneo con ese nombre");
        }
        return torneoMongoRepo.save(TorneoMongoMapper.toDocument(dto));
    }

    public List<TorneoDocument> findAll() {
        return torneoMongoRepo.findAll();
    }

    public TorneoDocument findById(String id) {
        return torneoMongoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));
    }

    public List<TorneoDocument> findByEstado(String estado) {
        return torneoMongoRepo.findByEstado(estado);
    }

    public TorneoDocument iniciarTorneo(String id) {
        TorneoDocument torneo = findById(id);

        if (!"PENDIENTE".equals(torneo.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden iniciar torneos PENDIENTES");
        }

        List<RondaDocument> rondas = rondaMongoRepo.findByTorneoIdOrderByNumeroRondaAsc(id);
        if (rondas.isEmpty()) {
            throw new IllegalArgumentException("El torneo debe tener al menos una ronda");
        }

        RondaDocument primeraRonda = rondas.get(0);
        primeraRonda.setEstado("IN_COURSE");
        primeraRonda.setFechaInicio(LocalDateTime.now());
        rondaMongoRepo.save(primeraRonda);

        torneo.setEstado("IN_COURSE");
        return torneoMongoRepo.save(torneo);
    }

    public TorneoDocument finalizarTorneo(String id) {
        TorneoDocument torneo = findById(id);

        if (!"IN_COURSE".equals(torneo.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden finalizar torneos en curso");
        }

        // Finalizar ronda activa
        List<RondaDocument> rondas = rondaMongoRepo.findByTorneoIdOrderByNumeroRondaAsc(id);
        rondas.stream()
                .filter(r -> "IN_COURSE".equals(r.getEstado()))
                .findFirst()
                .ifPresent(r -> {
                    r.setEstado("FINISHED");
                    rondaMongoRepo.save(r);
                });

        // Cambiar estado
        torneo.setEstado("FINISHED");
        torneo = torneoMongoRepo.save(torneo);

        // Calcular podio
        calcularPodio(id);

        return torneo;
    }

    private void calcularPodio(String torneoId) {
        List<PuntuacionDocument> todas = puntuacionMongoRepo.findByTorneoId(torneoId);

        // Agrupar por usuarioId y sumar puntajes
        Map<Long, Integer> sumaPorUsuario = new LinkedHashMap<>();
        Map<Long, String> nombrePorUsuario = new LinkedHashMap<>();

        for (PuntuacionDocument p : todas) {
            sumaPorUsuario.merge(p.getUsuarioId(), p.getPuntajeTotal(), Integer::sum);
            nombrePorUsuario.putIfAbsent(p.getUsuarioId(), p.getNombreArquero());
        }

        // Ordenar por puntaje descendente
        List<Map.Entry<Long, Integer>> ranking = sumaPorUsuario.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Actualizar participaciones con posición final
        int posicion = 1;
        for (Map.Entry<Long, Integer> entry : ranking) {
            final Long usuarioId = entry.getKey();
            final int puntajeTotal = entry.getValue();
            final int posicionFinal = posicion;

            participacionMongoRepo.findByTorneoIdAndUsuarioId(torneoId, usuarioId)
                    .ifPresent(part -> {
                        part.setPuntajeFinal(puntajeTotal);
                        part.setPosicionFinal(posicionFinal);
                        participacionMongoRepo.save(part);
                    });
            posicion++;
        }
    }
}