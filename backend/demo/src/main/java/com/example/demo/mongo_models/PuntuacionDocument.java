package com.example.demo.mongo_models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "puntuaciones")
@CompoundIndex(name = "uk_puntuacion_torneo_ronda_usuario", def = "{'torneoId': 1, 'rondaId': 1, 'usuarioId': 1}", unique = true)
public class PuntuacionDocument {

    @Id
    private String id;

    private String torneoId;
    private String rondaId;
    private Long usuarioId;
    private String nombreArquero;
    private String nombreTorneo;
    private Integer numeroRonda;
    private String categoria;
    private List<Integer> flechas = new ArrayList<>();
    private Integer puntajeTotal = 0;
    private String posicionArquero;
    private String posicionDiana;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}