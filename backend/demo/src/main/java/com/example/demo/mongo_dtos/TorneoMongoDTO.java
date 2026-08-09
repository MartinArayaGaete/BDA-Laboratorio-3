package com.example.demo.mongo_dtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TorneoMongoDTO {
    private String nombre;
    private Long sqlIdTorneo;
    private LocalDate fechaInicio;
    private LocalDate fechaTermino;
    private Integer plazasMax = 20;
    private Long categoriaDistanciaId;
    private Long categoriaDianaId;
}