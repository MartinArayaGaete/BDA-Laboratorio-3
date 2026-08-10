package com.example.demo.mongo_services;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PipelineEstadisticasService {

    private final MongoTemplate mongoTemplate;

    public PipelineEstadisticasService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Document> ejecutarPipelineRendimiento() {
        List<Document> pipeline = List.of(
                new Document("$project", new Document()
                        .append("torneoId", 1)
                        .append("nombreTorneo", 1)
                        .append("categoria", 1)
                        .append("usuarioId", 1)
                        .append("nombreArquero", 1)
                        .append("numeroRonda", 1)
                        .append("puntajeTotal", 1)
                ),
                new Document("$facet", new Document()
                        .append("rendimientoPorTorneo", List.of(
                                new Document("$group", new Document()
                                        .append("_id", new Document()
                                                .append("torneoId", "$torneoId")
                                                .append("usuarioId", "$usuarioId"))
                                        .append("nombreTorneo", new Document("$first", "$nombreTorneo"))
                                        .append("nombreArquero", new Document("$first", "$nombreArquero"))
                                        .append("promedioPuntaje", new Document("$avg", "$puntajeTotal"))
                                        .append("mejorRonda", new Document("$top", new Document()
                                                .append("sortBy", new Document("puntajeTotal", -1).append("numeroRonda", 1))
                                                .append("output", new Document()
                                                        .append("numeroRonda", "$numeroRonda")
                                                        .append("puntaje", "$puntajeTotal"))))
                                        .append("rondasRegistradas", new Document("$sum", 1))
                                ),
                                new Document("$project", new Document()
                                        .append("_id", 0)
                                        .append("torneoId", "$_id.torneoId")
                                        .append("usuarioId", "$_id.usuarioId")
                                        .append("nombreTorneo", 1)
                                        .append("nombreArquero", 1)
                                        .append("promedioPuntaje", 1)
                                        .append("mejorRonda", 1)
                                        .append("rondasRegistradas", 1)
                                ),
                                new Document("$sort", new Document()
                                        .append("torneoId", 1)
                                        .append("promedioPuntaje", -1)
                                        .append("mejorRonda.puntaje", -1)
                                        .append("usuarioId", 1)
                                )
                        ))
                        .append("rendimientoPorCategoria", List.of(
                                new Document("$group", new Document()
                                        .append("_id", new Document()
                                                .append("categoria", "$categoria")
                                                .append("usuarioId", "$usuarioId"))
                                        .append("nombreArquero", new Document("$first", "$nombreArquero"))
                                        .append("promedioPuntaje", new Document("$avg", "$puntajeTotal"))
                                        .append("mejorRonda", new Document("$top", new Document()
                                                .append("sortBy", new Document("puntajeTotal", -1).append("numeroRonda", 1))
                                                .append("output", new Document()
                                                        .append("numeroRonda", "$numeroRonda")
                                                        .append("puntaje", "$puntajeTotal"))))
                                        .append("rondasConsideradas", new Document("$sum", 1))
                                ),
                                new Document("$project", new Document()
                                        .append("_id", 0)
                                        .append("categoria", "$_id.categoria")
                                        .append("usuarioId", "$_id.usuarioId")
                                        .append("nombreArquero", 1)
                                        .append("promedioPuntaje", 1)
                                        .append("mejorRonda", 1)
                                        .append("rondasConsideradas", 1)
                                ),
                                new Document("$sort", new Document()
                                        .append("categoria", 1)
                                        .append("promedioPuntaje", -1)
                                        .append("mejorRonda.puntaje", -1)
                                        .append("usuarioId", 1)
                                )
                        ))
                        .append("distribucionPorRendimiento", List.of(
                                new Document("$group", new Document()
                                        .append("_id", new Document()
                                                .append("categoria", "$categoria")
                                                .append("usuarioId", "$usuarioId"))
                                        .append("nombreArquero", new Document("$first", "$nombreArquero"))
                                        .append("promedioPuntaje", new Document("$avg", "$puntajeTotal"))
                                        .append("rondasConsideradas", new Document("$sum", 1))
                                ),
                                new Document("$bucket", new Document()
                                        .append("groupBy", "$promedioPuntaje")
                                        .append("boundaries", List.of(0, 15, 30, 45, 61))
                                        .append("default", "fuera_de_rango")
                                        .append("output", new Document()
                                                .append("cantidadDesempenos", new Document("$sum", 1))
                                                .append("arqueros", new Document("$push", new Document()
                                                        .append("categoria", "$_id.categoria")
                                                        .append("usuarioId", "$_id.usuarioId")
                                                        .append("nombreArquero", "$nombreArquero")
                                                        .append("promedioPuntaje", "$promedioPuntaje")
                                                        .append("rondasConsideradas", "$rondasConsideradas")
                                                ))
                                        )
                                )
                        ))
                )
        );

        return mongoTemplate.getCollection("puntuaciones")
                .aggregate(pipeline)
                .into(new ArrayList<>());
    }
}