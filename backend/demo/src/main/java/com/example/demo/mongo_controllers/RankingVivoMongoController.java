package com.example.demo.mongo_controllers;

import com.example.demo.mongo_models.RankingVivoDocument;
import com.example.demo.mongo_services.RankingVivoMongoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mongo/ranking")
@CrossOrigin(origins = "*")
public class RankingVivoMongoController {

    private final RankingVivoMongoService service;

    public RankingVivoMongoController(RankingVivoMongoService service) {
        this.service = service;
    }

    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<RankingVivoDocument>> getRanking(@PathVariable String torneoId) {
        return ResponseEntity.ok(service.getRanking(torneoId));
    }
}
