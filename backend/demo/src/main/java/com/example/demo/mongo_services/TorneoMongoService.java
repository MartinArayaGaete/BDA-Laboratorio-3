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
import com.example.demo.models.Categoria;
import com.example.demo.models.CategoriaDiana;
import com.example.demo.repositories.CategoriaDianaRepository;
import com.example.demo.repositories.CategoriaRepository;
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
    private final CategoriaRepository categoriaSqlRepo;
    private final CategoriaDianaRepository categoriaDianaSqlRepo;

    public TorneoMongoService(TorneoMongoRepository torneoMongoRepo,
                              RondaMongoRepository rondaMongoRepo,
                              PuntuacionMongoRepository puntuacionMongoRepo,
                              ParticipacionMongoRepository participacionMongoRepo,
                              CategoriaRepository categoriaSqlRepo,
                              CategoriaDianaRepository categoriaDianaSqlRepo) {
        this.torneoMongoRepo = torneoMongoRepo;
        this.rondaMongoRepo = rondaMongoRepo;
        this.puntuacionMongoRepo = puntuacionMongoRepo;
        this.participacionMongoRepo = participacionMongoRepo;
        this.categoriaSqlRepo = categoriaSqlRepo;
        this.categoriaDianaSqlRepo = categoriaDianaSqlRepo;
    }

    public TorneoDocument crear(TorneoMongoDTO dto) {
        if (torneoMongoRepo.existsByNombre(dto.getNombre()))
            throw new IllegalArgumentException("Ya existe un torneo con ese nombre");

        TorneoDocument doc = TorneoMongoMapper.toDocument(dto);

        if (dto.getCategoriaDistanciaId() != null) {
            Categoria c = categoriaSqlRepo.buscarPorId(dto.getCategoriaDistanciaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría distancia no encontrada"));
            TorneoDocument.CategoriaDistanciaEmbedded emb = new TorneoDocument.CategoriaDistanciaEmbedded();
            emb.setNombre(c.getNombreCategoria());
            emb.setDistanciaTiro(c.getDistanciaTiro() != null ? c.getDistanciaTiro() : 18);
            doc.setCategoriaDistancia(emb);
        }

        if (dto.getCategoriaDianaId() != null) {
            CategoriaDiana cd = categoriaDianaSqlRepo.buscarPorId(dto.getCategoriaDianaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría diana no encontrada"));
            TorneoDocument.CategoriaDianaEmbedded emb = new TorneoDocument.CategoriaDianaEmbedded();
            emb.setNombre(cd.getNombreCategoriaDiana());
            emb.setPuntajeMinimo(cd.getPuntajeMinimo() != null ? cd.getPuntajeMinimo() : 5);
            doc.setCategoriaDiana(emb);
        }

        return torneoMongoRepo.save(doc);
    }

    public List<TorneoDocument> findAll() { return torneoMongoRepo.findAll(); }
    public TorneoDocument findById(String id) { return torneoMongoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado")); }
    public List<TorneoDocument> findByEstado(String e) { return torneoMongoRepo.findByEstado(e); }

    public TorneoDocument iniciarTorneo(String id) {
        TorneoDocument torneo = findById(id);
        if (!"PENDIENTE".equals(torneo.getEstado())) throw new IllegalArgumentException("Solo se pueden iniciar torneos PENDIENTES");
        List<RondaDocument> rondas = rondaMongoRepo.findByTorneoIdOrderByNumeroRondaAsc(id);
        if (rondas.isEmpty()) throw new IllegalArgumentException("El torneo debe tener al menos una ronda");
        RondaDocument primera = rondas.get(0);
        primera.setEstado("IN_COURSE");
        primera.setFechaInicio(LocalDateTime.now());
        rondaMongoRepo.save(primera);
        torneo.setEstado("IN_COURSE");
        return torneoMongoRepo.save(torneo);
    }

    public TorneoDocument finalizarTorneo(String id) {
        TorneoDocument torneo = findById(id);
        if (!"IN_COURSE".equals(torneo.getEstado())) throw new IllegalArgumentException("Solo se pueden finalizar torneos en curso");
        List<RondaDocument> rondas = rondaMongoRepo.findByTorneoIdOrderByNumeroRondaAsc(id);
        rondas.stream().filter(r -> "IN_COURSE".equals(r.getEstado())).findFirst().ifPresent(r -> {
            r.setEstado("FINISHED"); rondaMongoRepo.save(r);
        });
        torneo.setEstado("FINISHED");
        torneo = torneoMongoRepo.save(torneo);
        calcularPodio(id);
        return torneo;
    }

    public RondaDocument siguienteRonda(String torneoId) {
        TorneoDocument torneo = findById(torneoId);
        if (!"IN_COURSE".equals(torneo.getEstado())) throw new IllegalArgumentException("El torneo no está en curso");
        List<RondaDocument> rondas = rondaMongoRepo.findByTorneoIdOrderByNumeroRondaAsc(torneoId);
        RondaDocument actual = rondas.stream().filter(r -> "IN_COURSE".equals(r.getEstado())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay ronda en curso"));
        actual.setEstado("FINISHED"); rondaMongoRepo.save(actual);
        RondaDocument siguiente = rondas.stream().filter(r -> "PENDIENTE".equals(r.getEstado())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay más rondas"));
        siguiente.setEstado("IN_COURSE"); siguiente.setFechaInicio(LocalDateTime.now());
        return rondaMongoRepo.save(siguiente);
    }

    private void calcularPodio(String torneoId) {
        List<PuntuacionDocument> todas = puntuacionMongoRepo.findByTorneoId(torneoId);
        Map<Long, Integer> suma = new LinkedHashMap<>();
        for (PuntuacionDocument p : todas) suma.merge(p.getUsuarioId(), p.getPuntajeTotal(), Integer::sum);
        List<Map.Entry<Long, Integer>> ranking = suma.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed()).collect(Collectors.toList());
        int pos = 1;
        for (Map.Entry<Long, Integer> e : ranking) {
            final int pf = pos;
            participacionMongoRepo.findByTorneoIdAndUsuarioId(torneoId, e.getKey()).ifPresent(part -> {
                part.setPuntajeFinal(e.getValue()); part.setPosicionFinal(pf); participacionMongoRepo.save(part);
            });
            pos++;
        }
    }
}