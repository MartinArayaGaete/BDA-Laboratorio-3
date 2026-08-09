package com.example.demo.mongo_repositories;

import com.example.demo.mongo_models.RankingVivoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RankingVivoMongoRepository extends MongoRepository<RankingVivoDocument, String> {
    List<RankingVivoDocument> findByTorneoIdOrderByPuntajeTotalDesc(String torneoId);
    Optional<RankingVivoDocument> findByTorneoIdAndUsuarioId(String torneoId, Long usuarioId);
}