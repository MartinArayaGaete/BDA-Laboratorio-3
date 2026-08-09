package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDiana {
    private Long idCategoriaDiana;
    private String nombreCategoriaDiana;
    private Integer puntajeMinimo;
}