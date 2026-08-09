package com.example.demo.repositories;

import com.example.demo.models.CategoriaDiana;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoriaDianaRepository {

    private final JdbcTemplate jdbcTemplate;

    public CategoriaDianaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CategoriaDiana> obtenerTodas() {
        String sql = "SELECT id_categoria_diana, nombre_categoria_diana, puntaje_minimo, puntaje_maximo FROM categoria_diana ORDER BY id_categoria_diana";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CategoriaDiana(
                rs.getLong("id_categoria_diana"),
                rs.getString("nombre_categoria_diana"),
                rs.getInt("puntaje_minimo"),
                rs.getInt("puntaje_maximo")
        ));
    }

    public Optional<CategoriaDiana> buscarPorId(Long id) {
        String sql = "SELECT id_categoria_diana, nombre_categoria_diana, puntaje_minimo, puntaje_maximo FROM categoria_diana WHERE id_categoria_diana = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new CategoriaDiana(
                    rs.getLong("id_categoria_diana"),
                    rs.getString("nombre_categoria_diana"),
                    rs.getInt("puntaje_minimo"),
                    rs.getInt("puntaje_maximo")
            ), id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public int crear(String nombre, int puntajeMinimo, int puntajeMaximo) {
        String sql = "INSERT INTO categoria_diana (nombre_categoria_diana, puntaje_minimo, puntaje_maximo) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, nombre, puntajeMinimo, puntajeMaximo);
    }

    public int actualizar(Long id, String nombre, int puntajeMinimo, int puntajeMaximo) {
        String sql = "UPDATE categoria_diana SET nombre_categoria_diana = ?, puntaje_minimo = ?, puntaje_maximo = ? WHERE id_categoria_diana = ?";
        return jdbcTemplate.update(sql, nombre, puntajeMinimo, puntajeMaximo, id);
    }

    public int eliminar(Long id) {
        String sql = "DELETE FROM categoria_diana WHERE id_categoria_diana = ?";
        return jdbcTemplate.update(sql, id);
    }
}