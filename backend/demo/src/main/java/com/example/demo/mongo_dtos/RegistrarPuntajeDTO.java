package com.example.demo.mongo_dtos;

import lombok.Data;
import java.util.List;

@Data
public class RegistrarPuntajeDTO {
    private String torneoId;
    private String rondaId;
    private Long usuarioId;
    private List<Integer> flechas;
    private String posicionArquero;
    private String posicionDiana;
}