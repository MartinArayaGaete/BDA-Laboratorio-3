package com.example.demo.services;

import com.example.demo.models.CategoriaDiana;
import com.example.demo.repositories.CategoriaDianaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class CategoriaDianaService {

    private final CategoriaDianaRepository repository;

    public CategoriaDianaService(CategoriaDianaRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaDiana> obtenerTodas() {
        return repository.obtenerTodas();
    }

    public CategoriaDiana buscarPorId(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría diana no encontrada"));
    }

    public void crear(CategoriaDiana categoria) {
        if (categoria.getNombreCategoriaDiana() == null || categoria.getNombreCategoriaDiana().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (categoria.getPuntajeMinimo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Puntaje mínimo es obligatorio");
        }
        if (categoria.getPuntajeMinimo() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Puntaje mínimo no puede ser negativo");
        }
        repository.crear(categoria.getNombreCategoriaDiana(), categoria.getPuntajeMinimo());
    }

    public void actualizar(Long id, CategoriaDiana categoria) {
        buscarPorId(id);
        repository.actualizar(id, categoria.getNombreCategoriaDiana(), categoria.getPuntajeMinimo());
    }

    public void eliminar(Long id) {
        buscarPorId(id);
        repository.eliminar(id);
    }
}