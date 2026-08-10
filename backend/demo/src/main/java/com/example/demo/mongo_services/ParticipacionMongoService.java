package com.example.demo.mongo_services;

import com.example.demo.mongo_models.ParticipacionDocument;
import com.example.demo.mongo_repositories.ParticipacionMongoRepository;
import com.example.demo.mongo_repositories.TorneoMongoRepository;
import com.example.demo.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ParticipacionMongoService {

    private final ParticipacionMongoRepository participacionMongoRepo;
    private final UsuarioRepository usuarioSqlRepo;
    private final TorneoMongoRepository torneoMongoRepo;

    public ParticipacionMongoService(ParticipacionMongoRepository participacionMongoRepo,
                                     UsuarioRepository usuarioSqlRepo,
                                     TorneoMongoRepository torneoMongoRepo) {
        this.participacionMongoRepo = participacionMongoRepo;
        this.usuarioSqlRepo = usuarioSqlRepo;
        this.torneoMongoRepo = torneoMongoRepo;
    }

    public ParticipacionDocument inscribir(String torneoId, Long usuarioId) {
        var usuario = usuarioSqlRepo.buscarPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        var torneo = torneoMongoRepo.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));
        if (participacionMongoRepo.existsByTorneoIdAndUsuarioId(torneoId, usuarioId))
            throw new IllegalArgumentException("El arquero ya está inscrito");
        if (torneo.getPlazasActual() >= torneo.getPlazasMax())
            throw new IllegalArgumentException("No hay plazas disponibles");

        torneo.setPlazasActual(torneo.getPlazasActual() + 1);
        torneoMongoRepo.save(torneo);

        ParticipacionDocument p = new ParticipacionDocument();
        p.setTorneoId(torneoId);
        p.setUsuarioId(usuario.getIdUsuario());
        p.setNombreArquero(usuario.getNombre());
        p.setNombreTorneo(torneo.getNombre());
        return participacionMongoRepo.save(p);
    }

    public List<ParticipacionDocument> findAll() { return participacionMongoRepo.findAll(); }
    public ParticipacionDocument findById(String id) { return participacionMongoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Participación no encontrada")); }
    public List<ParticipacionDocument> findByTorneo(String t) { return participacionMongoRepo.findByTorneoId(t); }
    public List<ParticipacionDocument> findByUsuario(Long u) { return participacionMongoRepo.findByUsuarioId(u); }

    public void desinscribir(String torneoId, Long usuarioId) {
        ParticipacionDocument p = participacionMongoRepo.findByTorneoIdAndUsuarioId(torneoId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Participación no encontrada"));
        participacionMongoRepo.delete(p);
        var torneo = torneoMongoRepo.findById(torneoId).orElse(null);
        if (torneo != null) {
            torneo.setPlazasActual(Math.max(0, torneo.getPlazasActual() - 1));
            torneoMongoRepo.save(torneo);
        }
    }
}