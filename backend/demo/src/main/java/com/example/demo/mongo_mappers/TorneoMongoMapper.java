package com.example.demo.mongo_mappers;

import com.example.demo.mongo_models.TorneoDocument;
import com.example.demo.mongo_dtos.TorneoMongoDTO;

public class TorneoMongoMapper {

    public static TorneoDocument toDocument(TorneoMongoDTO dto) {
        TorneoDocument doc = new TorneoDocument();
        doc.setNombre(dto.getNombre());
        doc.setFechaInicio(dto.getFechaInicio());
        doc.setFechaTermino(dto.getFechaTermino());
        doc.setPlazasMax(dto.getPlazasMax() != null ? dto.getPlazasMax() : 20);

        TorneoDocument.CategoriaEmbedded cat = new TorneoDocument.CategoriaEmbedded();
        cat.setNombre(dto.getCategoriaNombre() != null ? dto.getCategoriaNombre() : "RECURVO");
        cat.setDistanciaTiro(dto.getDistanciaTiro() != null ? dto.getDistanciaTiro() : 18);
        doc.setCategoria(cat);

        return doc;
    }
}