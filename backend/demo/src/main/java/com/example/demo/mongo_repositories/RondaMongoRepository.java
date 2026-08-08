package com.example.demo.mongo_repositories;

import com.example.demo.mongo_models.RondaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RondaMongoRepository extends MongoRepository<RondaDocument, String> {

    List<RondaDocument> findByTorneoIdOrderByNumeroRondaAsc(String torneoId);
    boolean existsByTorneoIdAndNumeroRonda(String torneoId, int numeroRonda);
}