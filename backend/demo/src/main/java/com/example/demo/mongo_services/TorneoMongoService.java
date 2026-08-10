package com.example.demo.mongo_services;

import com.example.demo.mongo_models.ParticipacionDocument;
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
    private final CategoriaRepository categoriaRepository;
    private final CategoriaDianaRepository categoriaDianaRepository;

    public TorneoMongoService(TorneoMongoRepository torneoMongoRepo,
                              RondaMongoRepository rondaMongoRepo,
                              PuntuacionMongoRepository puntuacionMongoRepo,
                              ParticipacionMongoRepository participacionMongoRepo,
                              CategoriaRepository categoriaRepository,
                              CategoriaDianaRepository categoriaDianaRepository) {
        this.torneoMongoRepo = torneoMongoRepo;
        this.rondaMongoRepo = rondaMongoRepo;
        this.puntuacionMongoRepo = puntuacionMongoRepo;
        this.participacionMongoRepo = participacionMongoRepo;
        this.categoriaRepository = categoriaRepository;
        this.categoriaDianaRepository = categoriaDianaRepository;
    }

    public TorneoDocument crear(TorneoMongoDTO dto) {
        if (torneoMongoRepo.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un torneo con ese nombre");
        }
        TorneoDocument doc = TorneoMongoMapper.toDocument(dto);
        completarCategoriaDesdeSql(doc);
        return torneoMongoRepo.save(doc);
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

    public TorneoDocument actualizarTorneo(String id, TorneoMongoDTO dto) {
        TorneoDocument torneo = findById(id);
        if (!"PENDIENTE".equals(torneo.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden editar torneos en estado PENDIENTE");
        }
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            if (!torneo.getNombre().equals(dto.getNombre()) &&
                    torneoMongoRepo.existsByNombre(dto.getNombre())) {
                throw new IllegalArgumentException("Ya existe un torneo con ese nombre");
            }
            torneo.setNombre(dto.getNombre());
        }
        if (dto.getFechaInicio() != null) torneo.setFechaInicio(dto.getFechaInicio());
        if (dto.getFechaTermino() != null) torneo.setFechaTermino(dto.getFechaTermino());
        if (dto.getPlazasMax() != null) {
            if (dto.getPlazasMax() < torneo.getPlazasActual()) {
                throw new IllegalArgumentException("No se puede reducir plazas por debajo de los inscritos actuales");
            }
            torneo.setPlazasMax(dto.getPlazasMax());
        }
        if (dto.getZonaCompetenciaGeoJSON() != null) torneo.setZonaCompetenciaGeoJSON(dto.getZonaCompetenciaGeoJSON());
        if (dto.getLineaTiroGeoJSON() != null) torneo.setLineaTiroGeoJSON(dto.getLineaTiroGeoJSON());
        if (dto.getCategoriaDistanciaId() != null) torneo.setCategoriaDistanciaId(dto.getCategoriaDistanciaId());
        if (dto.getCategoriaDianaId() != null) torneo.setCategoriaDianaId(dto.getCategoriaDianaId());
        completarCategoriaDesdeSql(torneo);
        return torneoMongoRepo.save(torneo);
    }

    public void eliminarTorneo(String id) {
        TorneoDocument torneo = findById(id);
        if (!"PENDIENTE".equals(torneo.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden eliminar torneos en estado PENDIENTE");
        }
        List<PuntuacionDocument> puntuaciones = puntuacionMongoRepo.findByTorneoId(id);
        if (!puntuaciones.isEmpty()) puntuacionMongoRepo.deleteAll(puntuaciones);
        List<ParticipacionDocument> participaciones = participacionMongoRepo.findByTorneoId(id);
        if (!participaciones.isEmpty()) participacionMongoRepo.deleteAll(participaciones);
        List<RondaDocument> rondas = rondaMongoRepo.findByTorneoIdOrderByNumeroRondaAsc(id);
        if (!rondas.isEmpty()) rondaMongoRepo.deleteAll(rondas);
        torneoMongoRepo.deleteById(id);
    }

    public TorneoDocument iniciarTorneo(String id) {
        TorneoDocument torneo = findById(id);
        if (!"PENDIENTE".equals(torneo.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden iniciar torneos PENDIENTES");
        }
        List<RondaDocument> rondas = rondaMongoRepo.findByTorneoIdOrderByNumeroRondaAsc(id);
        if (rondas.isEmpty()) throw new IllegalArgumentException("El torneo debe tener al menos una ronda");
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
        rondas.stream().filter(r -> "IN_COURSE".equals(r.getEstado())).findFirst().ifPresent(r -> {
            r.setEstado("FINISHED");
            rondaMongoRepo.save(r);
        });
        torneo.setEstado("FINISHED");
        torneo = torneoMongoRepo.save(torneo);
        calcularPodio(id);
        return torneo;
    }

    private void completarCategoriaDesdeSql(TorneoDocument torneo) {
        if (torneo.getCategoriaDistanciaId() != null) {
            Categoria categoria = categoriaRepository.buscarPorId(torneo.getCategoriaDistanciaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada: " + torneo.getCategoriaDistanciaId()));
            TorneoDocument.CategoriaDistanciaEmbedded categoriaDistancia = new TorneoDocument.CategoriaDistanciaEmbedded();
            categoriaDistancia.setNombre(categoria.getNombreCategoria());
            categoriaDistancia.setDistanciaTiro(categoria.getDistanciaTiro() != null ? categoria.getDistanciaTiro() : 18);
            torneo.setCategoriaDistancia(categoriaDistancia);
        }
        if (torneo.getCategoriaDianaId() != null) {
            CategoriaDiana categoriaDiana = categoriaDianaRepository.buscarPorId(torneo.getCategoriaDianaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria diana no encontrada: " + torneo.getCategoriaDianaId()));
            TorneoDocument.CategoriaDianaEmbedded categoriaDianaEmbedded = new TorneoDocument.CategoriaDianaEmbedded();
            categoriaDianaEmbedded.setNombre(categoriaDiana.getNombreCategoriaDiana());
            categoriaDianaEmbedded.setPuntajeMinimo(categoriaDiana.getPuntajeMinimo() != null ? categoriaDiana.getPuntajeMinimo() : 5);
            torneo.setCategoriaDiana(categoriaDianaEmbedded);
        }
    }

    private void calcularPodio(String torneoId) {
        List<PuntuacionDocument> todas = puntuacionMongoRepo.findByTorneoId(torneoId);
        Map<Long, Integer> sumaPorUsuario = new LinkedHashMap<>();
        for (PuntuacionDocument p : todas) {
            sumaPorUsuario.merge(p.getUsuarioId(), p.getPuntajeTotal(), Integer::sum);
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
        RondaDocument actual = rondas.stream()
                .filter(r -> "IN_COURSE".equals(r.getEstado()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay ronda en curso"));
        actual.setEstado("FINISHED");
        rondaMongoRepo.save(actual);
        RondaDocument siguiente = rondas.stream()
                .filter(r -> "PENDIENTE".equals(r.getEstado()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay más rondas"));
        siguiente.setEstado("IN_COURSE");
        siguiente.setFechaInicio(LocalDateTime.now());
        return rondaMongoRepo.save(siguiente);
    }

    public List<Map<String, Object>> obtenerPosicionesRonda(String torneoId, int numeroRonda) {
        List<PuntuacionDocument> puntuaciones = puntuacionMongoRepo
                .findByTorneoIdAndNumeroRonda(torneoId, numeroRonda);
        return puntuaciones.stream()
                .filter(p -> p.getPosicionArquero() != null)
                .map(p -> Map.<String, Object>of(
                        "id_usuario", p.getUsuarioId(),
                        "nombre", p.getNombreArquero(),
                        "posicion_arquero", p.getPosicionArquero(),
                        "posicion_diana", p.getPosicionDiana(),
                        "puntaje_ronda", p.getPuntajeTotal()))
                .toList();
    }

    public Map<String, Object> obtenerPosicionArqueroEnRonda(String torneoId, int numeroRonda, Long usuarioId) {
        List<PuntuacionDocument> puntuaciones = puntuacionMongoRepo
                .findByTorneoIdAndNumeroRondaAndUsuarioId(torneoId, numeroRonda, usuarioId);
        if (puntuaciones.isEmpty()) throw new IllegalArgumentException("Posición no encontrada");
        PuntuacionDocument p = puntuaciones.get(0);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id_usuario", p.getUsuarioId());
        result.put("nombre", p.getNombreArquero());
        result.put("posicion_arquero", p.getPosicionArquero());
        result.put("posicion_diana", p.getPosicionDiana());
        result.put("puntaje_ronda", p.getPuntajeTotal());
        result.put("flechas", p.getFlechas());
        return result;
    }
}