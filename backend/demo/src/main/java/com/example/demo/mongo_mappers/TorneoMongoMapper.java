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
        doc.setCategoriaDistanciaId(dto.getCategoriaDistanciaId());
        doc.setCategoriaDianaId(dto.getCategoriaDianaId());
        doc.setZonaCompetenciaGeoJSON(dto.getZonaCompetenciaGeoJSON());
        doc.setLineaTiroGeoJSON(dto.getLineaTiroGeoJSON());
        return doc;
    }
}