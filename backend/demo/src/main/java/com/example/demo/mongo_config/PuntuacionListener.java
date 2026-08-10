package com.example.demo.mongo_config;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MergeOperation;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.MessageListener;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.data.mongodb.core.query.Criteria;

@Configuration
public class PuntuacionListener {

    private final MongoTemplate mongoTemplate;
    private final MessageListenerContainer listenerContainer;

    public PuntuacionListener(MongoTemplate mongoTemplate, MessageListenerContainer listenerContainer) {
        this.mongoTemplate = mongoTemplate;
        this.listenerContainer = listenerContainer;
    }

    @PostConstruct
    public void registrarListener() {
        ChangeStreamRequest.ChangeStreamRequestBuilder<Document> builder = ChangeStreamRequest.builder();

        ChangeStreamRequest<Document> request = builder
                .collection("puntuaciones")
                .filter(Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operationType").is("insert"))
                ))
                .publishTo((MessageListener<ChangeStreamDocument<Document>, Document>) message -> {
                    Document document = message.getBody();

                    if (document == null) {
                        return;
                    }

                    Long usuarioId = document.getLong("usuarioId");
                    String torneoId = document.getString("torneoId");

                    if (usuarioId != null) {
                        ejecutarMergeRanking(usuarioId,torneoId);
                    }
                })
                .build();

        listenerContainer.register(request, Document.class);
    }

    private void ejecutarMergeRanking(Long usuarioId, String torneoId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("usuarioId").is(usuarioId).and("torneoId").is(torneoId)),
                Aggregation.group("usuarioId", "torneoId")
                        .sum("puntajeTotal").as("puntajeTotal"),
                Aggregation.project("puntajeTotal")
                        .and("_id.usuarioId").as("usuarioId")
                        .and("_id.torneoId").as("torneoId")
                        .andExclude("_id"),
                MergeOperation.builder()
                        .intoCollection("ranking_vivo")
                        .on("torneoId","usuarioId")
                        .whenMatched(MergeOperation.WhenDocumentsMatch.mergeDocuments())
                        .whenNotMatched(MergeOperation.WhenDocumentsDontMatch.insertNewDocument())
                        .build()
        );

        mongoTemplate.aggregate(aggregation, "puntuaciones", Document.class);
    }
}
