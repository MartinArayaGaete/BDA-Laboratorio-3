package com.example.demo.mongo_dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TorneoMongoDTO {

    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaTermino;
    private Integer plazasMax = 20; // si es null queda en 20 plazas
    private String categoriaNombre = "RECURVO";
    private Integer distanciaTiro = 18;
}