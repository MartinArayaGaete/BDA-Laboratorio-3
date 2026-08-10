package com.example.demo.mongo_repositories;

import com.example.demo.mongo_models.TorneoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TorneoMongoRepository extends MongoRepository<TorneoDocument, String> {
    List<TorneoDocument> findByEstado(String estado);
    boolean existsByNombre(String nombre);
}