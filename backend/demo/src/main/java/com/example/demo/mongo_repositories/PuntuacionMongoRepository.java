package com.example.demo.mongo_repositories;

import com.example.demo.mongo_models.PuntuacionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PuntuacionMongoRepository extends MongoRepository<PuntuacionDocument, String> {

    Optional<PuntuacionDocument> findByTorneoIdAndRondaIdAndUsuarioId(String torneoId, String rondaId, Long usuarioId);
    List<PuntuacionDocument> findByTorneoId(String torneoId);
    List<PuntuacionDocument> findByRondaId(String rondaId);
    List<PuntuacionDocument> findByUsuarioId(Long usuarioId);
    List<PuntuacionDocument> findByTorneoIdOrderByPuntajeTotalDesc(String torneoId);
}