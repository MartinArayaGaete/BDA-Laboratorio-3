package com.example.demo.mongo_models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rondas")
public class RondaDocument {

    @Id
    private String id;

    private String torneoId;
    private int numeroRonda;
    private String estado = "PENDIENTE";

    private Long postgisZonaId;
    private LocalDateTime fechaInicio;
    private LocalDateTime createdAt = LocalDateTime.now();
}