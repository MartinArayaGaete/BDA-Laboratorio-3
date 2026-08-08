package com.example.demo.mongo_services;

import com.example.demo.mongo_models.PuntuacionDocument;
import com.example.demo.mongo_repositories.PuntuacionMongoRepository;
import com.example.demo.mongo_repositories.TorneoMongoRepository;
import com.example.demo.mongo_repositories.RondaMongoRepository;
import com.example.demo.mongo_repositories.ParticipacionMongoRepository;
import com.example.demo.repositories.UsuarioRepository;
import com.example.demo.models.Usuario;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PuntuacionMongoService {

    private final PuntuacionMongoRepository puntuacionMongoRepo;
    private final UsuarioRepository usuarioSqlRepo;
    private final TorneoMongoRepository torneoMongoRepo;
    private final RondaMongoRepository rondaMongoRepo;
    private final ParticipacionMongoRepository participacionMongoRepo;

    public PuntuacionMongoService(PuntuacionMongoRepository puntuacionMongoRepo,
                                  UsuarioRepository usuarioSqlRepo,
                                  TorneoMongoRepository torneoMongoRepo,
                                  RondaMongoRepository rondaMongoRepo,
                                  ParticipacionMongoRepository participacionMongoRepo) {
        this.puntuacionMongoRepo = puntuacionMongoRepo;
        this.usuarioSqlRepo = usuarioSqlRepo;
        this.torneoMongoRepo = torneoMongoRepo;
        this.rondaMongoRepo = rondaMongoRepo;
        this.participacionMongoRepo = participacionMongoRepo;
    }

    private void validarDatosEntrada(PuntuacionDocument doc) {
        if (doc.getTorneoId() == null || doc.getTorneoId().isBlank()) {
            throw new IllegalArgumentException("El ID del torneo es obligatorio");
        }
        if (doc.getRondaId() == null || doc.getRondaId().isBlank()) {
            throw new IllegalArgumentException("El ID de la ronda es obligatorio");
        }
        if (doc.getUsuarioId() == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }
        if (doc.getFlechas() == null || doc.getFlechas().isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos una flecha");
        }
        for (int puntaje : doc.getFlechas()) {
            if (puntaje < 0 || puntaje > 10) {
                throw new IllegalArgumentException("Cada flecha debe tener puntaje entre 0 y 10");
            }
        }
    }

    public PuntuacionDocument guardarOActualizar(PuntuacionDocument doc) {
        // 1. Validar datos de entrada
        validarDatosEntrada(doc);

        // 2. SQL: Validar que el usuario existe
        Usuario usuario = usuarioSqlRepo.buscarPorId(doc.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en el sistema"));

        // 3. MongoDB: Validar torneo
        var torneo = torneoMongoRepo.findById(doc.getTorneoId())
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));
        if (!"IN_COURSE".equals(torneo.getEstado())) {
            throw new IllegalArgumentException("El torneo no está en curso. Debe iniciar el torneo primero");
        }

        // 4. MongoDB: Validar ronda
        var ronda = rondaMongoRepo.findById(doc.getRondaId())
                .orElseThrow(() -> new IllegalArgumentException("Ronda no encontrada"));
        if (!"IN_COURSE".equals(ronda.getEstado())) {
            throw new IllegalArgumentException("La ronda no está en curso. Debe iniciar la ronda primero");
        }

        // 5. MongoDB: Validar inscripción
        if (!participacionMongoRepo.existsByTorneoIdAndUsuarioId(doc.getTorneoId(), doc.getUsuarioId())) {
            throw new IllegalArgumentException("El arquero no está inscrito en este torneo. Debe inscribirlo primero");
        }

        // 6. Upsert
        PuntuacionDocument target = puntuacionMongoRepo
                .findByTorneoIdAndRondaIdAndUsuarioId(doc.getTorneoId(), doc.getRondaId(), doc.getUsuarioId())
                .orElseGet(PuntuacionDocument::new);

        if (target.getId() == null) {
            target.setCreatedAt(LocalDateTime.now());
        }

        target.setTorneoId(doc.getTorneoId());
        target.setRondaId(doc.getRondaId());
        target.setUsuarioId(usuario.getIdUsuario());
        target.setNombreArquero(usuario.getNombre());
        target.setNombreTorneo(torneo.getNombre());
        target.setNumeroRonda(ronda.getNumeroRonda());
        target.setCategoria(torneo.getCategoria().getNombre());
        target.setFlechas(doc.getFlechas());
        target.setPuntajeTotal(doc.getFlechas().stream().mapToInt(Integer::intValue).sum());
        target.setPosicionArquero(doc.getPosicionArquero());
        target.setPosicionDiana(doc.getPosicionDiana());
        target.setUpdatedAt(LocalDateTime.now());

        return puntuacionMongoRepo.save(target);
    }

    public List<PuntuacionDocument> findAll() {
        return puntuacionMongoRepo.findAll();
    }

    public PuntuacionDocument findById(String id) {
        return puntuacionMongoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Puntuación no encontrada"));
    }

    public List<PuntuacionDocument> findByTorneo(String torneoId) {
        return puntuacionMongoRepo.findByTorneoId(torneoId);
    }

    public List<PuntuacionDocument> findByRonda(String rondaId) {
        return puntuacionMongoRepo.findByRondaId(rondaId);
    }

    public List<PuntuacionDocument> findByUsuario(Long usuarioId) {
        return puntuacionMongoRepo.findByUsuarioId(usuarioId);
    }

    public List<PuntuacionDocument> getRanking(String torneoId) {
        return puntuacionMongoRepo.findByTorneoIdOrderByPuntajeTotalDesc(torneoId);
    }

    public void deleteById(String id) {
        findById(id);
        puntuacionMongoRepo.deleteById(id);
    }
}