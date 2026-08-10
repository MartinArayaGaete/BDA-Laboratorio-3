package com.example.demo.mongo_services;

import com.example.demo.mongo_models.PuntuacionDocument;
import com.example.demo.mongo_models.TorneoDocument;
import com.example.demo.mongo_models.RankingVivoDocument;
import com.example.demo.mongo_models.ParticipacionDocument;
import com.example.demo.mongo_repositories.PuntuacionMongoRepository;
import com.example.demo.mongo_repositories.TorneoMongoRepository;
import com.example.demo.mongo_repositories.RondaMongoRepository;
import com.example.demo.mongo_repositories.ParticipacionMongoRepository;
import com.example.demo.mongo_repositories.RankingVivoMongoRepository;
import com.example.demo.repositories.UsuarioRepository;
import com.example.demo.models.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PuntuacionMongoService {

    private final PuntuacionMongoRepository puntuacionMongoRepo;
    private final UsuarioRepository usuarioSqlRepo;
    private final TorneoMongoRepository torneoMongoRepo;
    private final RondaMongoRepository rondaMongoRepo;
    private final ParticipacionMongoRepository participacionMongoRepo;
    private final RankingVivoMongoRepository rankingVivoMongoRepo;
    private final GeospatialMongoService geospatialMongoService;

    public PuntuacionMongoService(PuntuacionMongoRepository puntuacionMongoRepo,
                                  UsuarioRepository usuarioSqlRepo,
                                  TorneoMongoRepository torneoMongoRepo,
                                  RondaMongoRepository rondaMongoRepo,
                                  ParticipacionMongoRepository participacionMongoRepo,
                                  RankingVivoMongoRepository rankingVivoMongoRepo,
                                  GeospatialMongoService geospatialMongoService) {
        this.puntuacionMongoRepo = puntuacionMongoRepo;
        this.usuarioSqlRepo = usuarioSqlRepo;
        this.torneoMongoRepo = torneoMongoRepo;
        this.rondaMongoRepo = rondaMongoRepo;
        this.participacionMongoRepo = participacionMongoRepo;
        this.rankingVivoMongoRepo = rankingVivoMongoRepo;
        this.geospatialMongoService = geospatialMongoService;
    }

    private void validarDatosEntrada(PuntuacionDocument doc) {
        if (doc.getTorneoId() == null || doc.getTorneoId().isBlank())
            throw new IllegalArgumentException("El ID del torneo es obligatorio");
        if (doc.getRondaId() == null || doc.getRondaId().isBlank())
            throw new IllegalArgumentException("El ID de la ronda es obligatorio");
        if (doc.getUsuarioId() == null)
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        if (doc.getFlechas() == null || doc.getFlechas().isEmpty())
            throw new IllegalArgumentException("Debe registrar al menos una flecha");
        for (int puntaje : doc.getFlechas())
            if (puntaje < 0 || puntaje > 10)
                throw new IllegalArgumentException("Cada flecha debe tener puntaje entre 0 y 10");
    }

    private Usuario validarUsuario(Long usuarioId) {
        return usuarioSqlRepo.buscarPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private TorneoDocument validarTorneoEnCurso(String torneoId) {
        var torneo = torneoMongoRepo.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));
        if (!"IN_COURSE".equals(torneo.getEstado()))
            throw new IllegalArgumentException("El torneo no está en curso");
        return torneo;
    }

    private void validarRondaEnCurso(String rondaId, String torneoId) {
        var ronda = rondaMongoRepo.findById(rondaId)
                .orElseThrow(() -> new IllegalArgumentException("Ronda no encontrada"));
        if (!"IN_COURSE".equals(ronda.getEstado()))
            throw new IllegalArgumentException("La ronda no está en curso");
        if (!ronda.getTorneoId().equals(torneoId))
            throw new IllegalArgumentException("La ronda no pertenece a este torneo");
    }

    private void validarInscripcion(String torneoId, Long usuarioId) {
        if (!participacionMongoRepo.existsByTorneoIdAndUsuarioId(torneoId, usuarioId))
            throw new IllegalArgumentException("El arquero no está inscrito");
    }

    private void validarReglamentoCategoria(PuntuacionDocument doc, TorneoDocument torneo) {
        if (torneo.getCategoriaDiana() != null) {
            int min = torneo.getCategoriaDiana().getPuntajeMinimo();
            for (int puntaje : doc.getFlechas())
                if (puntaje < min)
                    throw new IllegalArgumentException("Puntaje " + puntaje + " menor al mínimo (" + min + ")");
        }
    }

    private void validarPosicionArquero(PuntuacionDocument doc, TorneoDocument torneo) {
        if (doc.getPosicionArquero() == null) return;
        if (torneo.getZonaCompetenciaGeoJSON() == null) return;
        if (!geospatialMongoService.validarPuntoEnPoligono(doc.getPosicionArquero(), torneo.getZonaCompetenciaGeoJSON()))
            throw new IllegalArgumentException("El arquero está fuera de la zona de competencia");
    }

    private void validarDistanciaTiro(PuntuacionDocument doc, TorneoDocument torneo) {
        if (doc.getPosicionArquero() == null || doc.getPosicionDiana() == null) return;
        if (torneo.getCategoriaDistancia() == null) return;
        double distancia = geospatialMongoService.calcularDistancia(doc.getPosicionArquero(), doc.getPosicionDiana());
        int maxDistancia = torneo.getCategoriaDistancia().getDistanciaTiro();
        if (distancia > maxDistancia)
            throw new IllegalArgumentException("Distancia excedida: " + Math.round(distancia) + "m (máx: " + maxDistancia + "m)");
    }

    private PuntuacionDocument ejecutarUpsert(PuntuacionDocument doc, Usuario usuario, TorneoDocument torneo) {
        PuntuacionDocument target = puntuacionMongoRepo
                .findByTorneoIdAndRondaIdAndUsuarioId(doc.getTorneoId(), doc.getRondaId(), doc.getUsuarioId())
                .orElseGet(PuntuacionDocument::new);
        if (target.getId() == null) target.setCreatedAt(LocalDateTime.now());
        target.setTorneoId(doc.getTorneoId());
        target.setRondaId(doc.getRondaId());
        target.setUsuarioId(usuario.getIdUsuario());
        target.setNombreArquero(usuario.getNombre());
        target.setNombreTorneo(torneo.getNombre());
        target.setNumeroRonda(doc.getNumeroRonda());
        target.setCategoria(torneo.getCategoriaDistancia() != null ? torneo.getCategoriaDistancia().getNombre() : null);
        target.setPuntajeMinimo(torneo.getCategoriaDiana() != null ? torneo.getCategoriaDiana().getPuntajeMinimo() : 0);
        target.setFlechas(doc.getFlechas());
        target.setPuntajeTotal(doc.getFlechas().stream().mapToInt(Integer::intValue).sum());
        target.setPosicionArquero(doc.getPosicionArquero());
        target.setPosicionDiana(doc.getPosicionDiana());
        target.setUpdatedAt(LocalDateTime.now());
        return puntuacionMongoRepo.save(target);
    }

    private void actualizarParticipacion(String torneoId, Long usuarioId) {
        List<PuntuacionDocument> todas = puntuacionMongoRepo.findByTorneoId(torneoId).stream()
                .filter(p -> p.getUsuarioId().equals(usuarioId)).toList();
        int total = todas.stream().mapToInt(PuntuacionDocument::getPuntajeTotal).sum();
        participacionMongoRepo.findByTorneoIdAndUsuarioId(torneoId, usuarioId).ifPresent(part -> {
            part.setPuntajeFinal(total);
            participacionMongoRepo.save(part);
        });
    }

    private void actualizarRankingVivo(String torneoId, Long usuarioId, String nombreArquero, String nombreTorneo, int puntajeTotal) {
        RankingVivoDocument rankingDoc = rankingVivoMongoRepo
                .findByTorneoIdAndUsuarioId(torneoId, usuarioId)
                .orElseGet(RankingVivoDocument::new);
        rankingDoc.setTorneoId(torneoId);
        rankingDoc.setUsuarioId(usuarioId);
        rankingDoc.setNombreArquero(nombreArquero);
        rankingDoc.setNombreTorneo(nombreTorneo);
        rankingDoc.setPuntajeTotal(puntajeTotal);
        rankingDoc.setUltimaActualizacion(LocalDateTime.now());
        rankingVivoMongoRepo.save(rankingDoc);

        List<RankingVivoDocument> ranking = rankingVivoMongoRepo.findByTorneoIdOrderByPuntajeTotalDesc(torneoId);
        int pos = 1;
        for (RankingVivoDocument rd : ranking) {
            rd.setPosicion(pos++);
            rankingVivoMongoRepo.save(rd);
        }
    }

    // ========== MÉTODOS PÚBLICOS ==========

    public PuntuacionDocument guardarOActualizar(PuntuacionDocument doc) {
        validarDatosEntrada(doc);
        Usuario usuario = validarUsuario(doc.getUsuarioId());
        TorneoDocument torneo = validarTorneoEnCurso(doc.getTorneoId());
        validarRondaEnCurso(doc.getRondaId(), doc.getTorneoId());
        validarInscripcion(doc.getTorneoId(), doc.getUsuarioId());
        return ejecutarUpsert(doc, usuario, torneo);
    }

    @Transactional
    public PuntuacionDocument guardarOActualizarConReglamento(PuntuacionDocument doc) {
        validarDatosEntrada(doc);
        Usuario usuario = validarUsuario(doc.getUsuarioId());
        TorneoDocument torneo = validarTorneoEnCurso(doc.getTorneoId());
        validarRondaEnCurso(doc.getRondaId(), doc.getTorneoId());
        validarInscripcion(doc.getTorneoId(), doc.getUsuarioId());
        validarReglamentoCategoria(doc, torneo);
        validarPosicionArquero(doc, torneo);
        validarDistanciaTiro(doc, torneo);

        PuntuacionDocument saved = ejecutarUpsert(doc, usuario, torneo);
        actualizarParticipacion(doc.getTorneoId(), doc.getUsuarioId());
        actualizarRankingVivo(doc.getTorneoId(), usuario.getIdUsuario(), usuario.getNombre(), torneo.getNombre(), saved.getPuntajeTotal());
        return saved;
    }

    public List<PuntuacionDocument> findAll() { return puntuacionMongoRepo.findAll(); }
    public PuntuacionDocument findById(String id) { return puntuacionMongoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Puntuación no encontrada")); }
    public List<PuntuacionDocument> findByTorneo(String t) { return puntuacionMongoRepo.findByTorneoId(t); }
    public List<PuntuacionDocument> findByRonda(String r) { return puntuacionMongoRepo.findByRondaId(r); }
    public List<PuntuacionDocument> findByUsuario(Long u) { return puntuacionMongoRepo.findByUsuarioId(u); }
    public List<PuntuacionDocument> getRanking(String t) { return puntuacionMongoRepo.findByTorneoIdOrderByPuntajeTotalDesc(t); }
    public void deleteById(String id) { findById(id); puntuacionMongoRepo.deleteById(id); }
}