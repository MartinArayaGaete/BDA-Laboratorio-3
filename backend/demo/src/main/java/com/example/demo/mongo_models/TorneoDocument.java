package com.example.demo.mongo_models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "torneos")
public class TorneoDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nombre;

    private String estado = "PENDIENTE";
    private LocalDate fechaInicio;
    private LocalDate fechaTermino;
    private int plazasMax;
    private int plazasActual = 0;

    private CategoriaEmbedded categoria;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaEmbedded {
        private String nombre = "RECURVO";
        private int distanciaTiro = 18;
    }
}