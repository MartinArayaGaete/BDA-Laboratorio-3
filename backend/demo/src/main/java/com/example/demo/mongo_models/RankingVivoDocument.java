package com.example.demo.mongo_models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ranking_vivo")
@CompoundIndex(name = "uk_ranking_vivo_torneo_usuario", def = "{'torneoId': 1, 'usuarioId': 1}", unique = true)
public class RankingVivoDocument {

    @Id
    private String id;

    private String torneoId;
    private Long usuarioId;
    private String nombreArquero;
    private String nombreTorneo;
    private int puntajeTotal;
    private int posicion;
    private LocalDateTime ultimaActualizacion = LocalDateTime.now();
}