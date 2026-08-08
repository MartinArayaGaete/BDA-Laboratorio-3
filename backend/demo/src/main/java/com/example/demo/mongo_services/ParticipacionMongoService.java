package com.example.demo.mongo_services;

import com.example.demo.mongo_models.ParticipacionDocument;
import com.example.demo.mongo_repositories.ParticipacionMongoRepository;
import com.example.demo.mongo_repositories.TorneoMongoRepository;
import com.example.demo.repositories.UsuarioRepository;
import com.example.demo.models.Usuario;
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
        // 1. SQL: Verificar que el usuario existe
        var usuario = usuarioSqlRepo.buscarPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en el sistema"));

        // 2. MongoDB: Verificar que el torneo existe
        var torneo = torneoMongoRepo.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        // 3. MongoDB: Verificar que no esté ya inscrito
        if (participacionMongoRepo.existsByTorneoIdAndUsuarioId(torneoId, usuarioId)) {
            throw new IllegalArgumentException("El arquero ya está inscrito en este torneo");
        }

        // 4. MongoDB: Crear participación
        ParticipacionDocument p = new ParticipacionDocument();
        p.setTorneoId(torneoId);
        p.setUsuarioId(usuario.getIdUsuario());
        p.setNombreArquero(usuario.getNombre());
        p.setNombreTorneo(torneo.getNombre());

        return participacionMongoRepo.save(p);
    }

    public List<ParticipacionDocument> findAll() {
        return participacionMongoRepo.findAll();
    }

    public ParticipacionDocument findById(String id) {
        return participacionMongoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Participación no encontrada"));
    }

    public List<ParticipacionDocument> findByTorneo(String torneoId) {
        return participacionMongoRepo.findByTorneoId(torneoId);
    }

    public List<ParticipacionDocument> findByUsuario(Long usuarioId) {
        return participacionMongoRepo.findByUsuarioId(usuarioId);
    }

    public void desinscribir(String torneoId, Long usuarioId) {
        ParticipacionDocument p = participacionMongoRepo
                .findByTorneoIdAndUsuarioId(torneoId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Participación no encontrada"));
        participacionMongoRepo.delete(p);
    }
}