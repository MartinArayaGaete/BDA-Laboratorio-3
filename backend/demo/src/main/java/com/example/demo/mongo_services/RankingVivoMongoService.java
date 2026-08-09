package com.example.demo.mongo_services;

import com.example.demo.mongo_models.RankingVivoDocument;
import com.example.demo.mongo_repositories.RankingVivoMongoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RankingVivoMongoService {

    private final RankingVivoMongoRepository repository;

    public RankingVivoMongoService(RankingVivoMongoRepository repository) {
        this.repository = repository;
    }

    public List<RankingVivoDocument> getRanking(String torneoId) {
        return repository.findByTorneoIdOrderByPuntajeTotalDesc(torneoId);
    }
}