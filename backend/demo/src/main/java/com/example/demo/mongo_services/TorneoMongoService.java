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
import com.example.demo.repositories.TorneoRepository;
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
    private final TorneoRepository torneoSqlRepo;
    private final CategoriaRepository categoriaSqlRepo;
    private final CategoriaDianaRepository categoriaDianaSqlRepo;

    public TorneoMongoService(TorneoMongoRepository torneoMongoRepo,
                              RondaMongoRepository rondaMongoRepo,
                              PuntuacionMongoRepository puntuacionMongoRepo,
                              ParticipacionMongoRepository participacionMongoRepo,
                              TorneoRepository torneoSqlRepo,
                              CategoriaRepository categoriaSqlRepo,
                              CategoriaDianaRepository categoriaDianaSqlRepo) {
        this.torneoMongoRepo = torneoMongoRepo;
        this.rondaMongoRepo = rondaMongoRepo;
        this.puntuacionMongoRepo = puntuacionMongoRepo;
        this.participacionMongoRepo = participacionMongoRepo;
        this.torneoSqlRepo = torneoSqlRepo;
        this.categoriaSqlRepo = categoriaSqlRepo;
        this.categoriaDianaSqlRepo = categoriaDianaSqlRepo;
    }

    public TorneoDocument crear(TorneoMongoDTO dto) {
        if (torneoMongoRepo.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un torneo con ese nombre");
        }

        TorneoDocument doc = TorneoMongoMapper.toDocument(dto);

        if (dto.getCategoriaDistanciaId() != null) {
            Categoria categoria = categoriaSqlRepo.buscarPorId(dto.getCategoriaDistanciaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría distancia no encontrada"));
            TorneoDocument.CategoriaDistanciaEmbedded emb = new TorneoDocument.CategoriaDistanciaEmbedded();
            emb.setNombre(categoria.getNombreCategoria());
            emb.setDistanciaTiro(categoria.getDistanciaTiro() != null ? categoria.getDistanciaTiro() : 18);
            doc.setCategoriaDistancia(emb);
        }

        if (dto.getCategoriaDianaId() != null) {
            CategoriaDiana categoriaDiana = categoriaDianaSqlRepo.buscarPorId(dto.getCategoriaDianaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría diana no encontrada"));
            TorneoDocument.CategoriaDianaEmbedded emb = new TorneoDocument.CategoriaDianaEmbedded();
            emb.setNombre(categoriaDiana.getNombreCategoriaDiana());
            emb.setPuntajeMinimo(categoriaDiana.getPuntajeMinimo() != null ? categoriaDiana.getPuntajeMinimo() : 5);
            doc.setCategoriaDiana(emb);
        }

        TorneoDocument saved = torneoMongoRepo.save(doc);
        if (dto.getSqlIdTorneo() != null) {
            return sincronizarZonasAmbientalesDesdeSql(saved.getId(), dto.getSqlIdTorneo());
        }
        return saved;
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

    public TorneoDocument sincronizarZonasAmbientalesDesdeSql(String mongoTorneoId, Long sqlIdTorneo) {
        TorneoDocument torneo = torneoMongoRepo.findById(mongoTorneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo Mongo no encontrado"));

        List<Map<String, Object>> zonasSql = torneoSqlRepo.obtenerClimasPorTorneo(sqlIdTorneo);

        List<TorneoDocument.ZonaAmbientalEmbedded> zonas = zonasSql.stream()
                .map(row -> new TorneoDocument.ZonaAmbientalEmbedded(
                        row.get("id_zona_ambiental") != null ? ((Number) row.get("id_zona_ambiental")).longValue() : null,
                        row.get("id_categoria_ambiental") != null ? ((Number) row.get("id_categoria_ambiental")).longValue() : null,
                        row.get("categoria_ambiental") != null ? row.get("categoria_ambiental").toString() : null
                ))
                .collect(Collectors.toList());

        torneo.setSqlIdTorneo(sqlIdTorneo);
        torneo.setZonasAmbientales(zonas);
        return torneoMongoRepo.save(torneo);
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

        List<RondaDocument> rondas = rondaMongoRepo.findByTorneoIdOrderByNumeroRondaAsc(id);
        rondas.stream()
                .filter(r -> "IN_COURSE".equals(r.getEstado()))
                .findFirst()
                .ifPresent(r -> {
                    r.setEstado("FINISHED");
                    rondaMongoRepo.save(r);
                });

        torneo.setEstado("FINISHED");
        torneo = torneoMongoRepo.save(torneo);

        calcularPodio(id);

        return torneo;
    }

    private void calcularPodio(String torneoId) {
        List<PuntuacionDocument> todas = puntuacionMongoRepo.findByTorneoId(torneoId);

        Map<Long, Integer> sumaPorUsuario = new LinkedHashMap<>();
        Map<Long, String> nombrePorUsuario = new LinkedHashMap<>();

        for (PuntuacionDocument p : todas) {
            sumaPorUsuario.merge(p.getUsuarioId(), p.getPuntajeTotal(), Integer::sum);
            nombrePorUsuario.putIfAbsent(p.getUsuarioId(), p.getNombreArquero());
        }

        List<Map.Entry<Long, Integer>> ranking = sumaPorUsuario.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

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

    public RondaDocument siguienteRonda(String torneoId) {
        TorneoDocument torneo = findById(torneoId);

        if (!"IN_COURSE".equals(torneo.getEstado())) {
            throw new IllegalArgumentException("El torneo no está en curso");
        }

        List<RondaDocument> rondas = rondaMongoRepo.findByTorneoIdOrderByNumeroRondaAsc(torneoId);

        // Finalizar ronda actual
        RondaDocument rondaActual = rondas.stream()
                .filter(r -> "IN_COURSE".equals(r.getEstado()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay ronda en curso"));
        rondaActual.setEstado("FINISHED");
        rondaMongoRepo.save(rondaActual);

        // Iniciar siguiente ronda
        RondaDocument siguiente = rondas.stream()
                .filter(r -> "PENDIENTE".equals(r.getEstado()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay más rondas"));
        siguiente.setEstado("IN_COURSE");
        siguiente.setFechaInicio(LocalDateTime.now());

        return rondaMongoRepo.save(siguiente);
    }


}