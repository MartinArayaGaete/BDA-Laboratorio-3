package com.example.demo.mongo_models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "torneos")
public class TorneoDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nombre;

    @Indexed(unique = true, sparse = true)
    private Long sqlIdTorneo;

    private String estado = "PENDIENTE";
    private LocalDate fechaInicio;
    private LocalDate fechaTermino;
    private int plazasMax;
    private int plazasActual = 0;

    private Long categoriaDistanciaId;
    private CategoriaDistanciaEmbedded categoriaDistancia;

    private Long categoriaDianaId;
    private CategoriaDianaEmbedded categoriaDiana;
    private List<ZonaAmbientalEmbedded> zonasAmbientales = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaDistanciaEmbedded {
        private String nombre = "RECURVO";
        private int distanciaTiro = 18;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZonaAmbientalEmbedded {
        private Long idZonaAmbiental;
        private Long idCategoriaAmbiental;
        private String categoriaAmbiental;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaDianaEmbedded {
        private String nombre = "ADULTO";
        private int puntajeMinimo = 5;
        private int puntajeMaximo = 10;
    }
}