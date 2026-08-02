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
@Document(collection = "participaciones")
public class ParticipacionDocument {

    @Id
    private String id;

    private String torneoId;
    private Long usuarioId;
    private String nombreArquero;
    private String nombreTorneo;
    private int puntajeFinal = 0;
    private Integer posicionFinal;
    private LocalDateTime inscritoEn = LocalDateTime.now();
}