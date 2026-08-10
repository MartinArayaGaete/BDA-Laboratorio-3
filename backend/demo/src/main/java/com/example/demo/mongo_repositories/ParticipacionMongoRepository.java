package com.example.demo.mongo_repositories;

import com.example.demo.mongo_models.ParticipacionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipacionMongoRepository extends MongoRepository<ParticipacionDocument, String> {

    List<ParticipacionDocument> findByTorneoId(String torneoId);
    List<ParticipacionDocument> findByUsuarioId(Long usuarioId);
    Optional<ParticipacionDocument> findByTorneoIdAndUsuarioId(String torneoId, Long usuarioId);
    boolean existsByTorneoIdAndUsuarioId(String torneoId, Long usuarioId);
    List<ParticipacionDocument> findByTorneoIdOrderByPosicionFinalAsc(String torneoId);
}