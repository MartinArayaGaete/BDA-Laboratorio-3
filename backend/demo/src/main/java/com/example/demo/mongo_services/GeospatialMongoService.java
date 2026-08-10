package com.example.demo.mongo_services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class GeospatialMongoService {

    private final JdbcTemplate jdbcTemplate;

    public GeospatialMongoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean validarPuntoEnPoligono(String puntoGeoJSON, String poligonoGeoJSON) {
        if (puntoGeoJSON == null || poligonoGeoJSON == null) return false;
        String sql = """
            SELECT ST_Contains(
                ST_SetSRID(ST_GeomFromGeoJSON(?), 4326),
                ST_SetSRID(ST_GeomFromGeoJSON(?), 4326)
            )
        """;
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, poligonoGeoJSON, puntoGeoJSON);
        return result != null && result;
    }

    public double calcularDistancia(String punto1GeoJSON, String punto2GeoJSON) {
        if (punto1GeoJSON == null || punto2GeoJSON == null) return 0.0;
        String sql = """
            SELECT ST_Distance(
                ST_SetSRID(ST_GeomFromGeoJSON(?), 4326)::geography,
                ST_SetSRID(ST_GeomFromGeoJSON(?), 4326)::geography
            )
        """;
        Double result = jdbcTemplate.queryForObject(sql, Double.class, punto1GeoJSON, punto2GeoJSON);
        return result != null ? result : 0.0;
    }
}