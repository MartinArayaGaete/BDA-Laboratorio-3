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
    private int plazasMax;
    private String categoriaNombre = "RECURVO";
    private int distanciaTiro = 18;
}