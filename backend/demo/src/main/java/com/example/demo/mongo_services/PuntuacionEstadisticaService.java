package com.example.demo.mongo_services;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class PuntuacionEstadisticaService {

    private final MongoTemplate mongoTemplate;

    public PuntuacionEstadisticaService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Document> obtenerEstadisticas() {

        Aggregation pipeline = newAggregation(

                // 1. Ordenamos para que la mejor ronda quede primero
                sort(
                        Sort.by(
                                Sort.Order.asc("torneoId"),
                                Sort.Order.asc("categoria"),
                                Sort.Order.asc("usuarioId"),
                                Sort.Order.desc("puntajeTotal")
                        )
                ),

                // 2. Agrupamos por torneo + categoría + arquero
                group("torneoId", "categoria", "usuarioId")
                        .first("nombreTorneo").as("nombreTorneo")
                        .first("nombreArquero").as("nombreArquero")
                        .avg("puntajeTotal").as("promedioPuntaje")
                        .first("puntajeTotal").as("mejorPuntaje")
                        .first("numeroRonda").as("mejorRonda"),

                // 3. Ordenamos los resultados
                sort(
                        Sort.by(
                                Sort.Order.asc("_id.torneoId"),
                                Sort.Order.asc("_id.categoria"),
                                Sort.Order.desc("promedioPuntaje")
                        )
                )
        );

        AggregationResults<Document> resultado =
                mongoTemplate.aggregate(
                        pipeline,
                        "puntuaciones",
                        Document.class
                );

        return resultado.getMappedResults();
    }
}
