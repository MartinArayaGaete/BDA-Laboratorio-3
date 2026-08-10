package com.example.demo.mongo_services;

import com.example.demo.mongo_models.ParticipacionDocument;
import com.example.demo.mongo_models.PuntuacionDocument;
import com.example.demo.mongo_repositories.ParticipacionMongoRepository;
import com.example.demo.mongo_repositories.PuntuacionMongoRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ArqueroMongoService {

    private final ParticipacionMongoRepository participacionMongoRepo;
    private final PuntuacionMongoRepository puntuacionMongoRepo;

    public ArqueroMongoService(ParticipacionMongoRepository participacionMongoRepo,
                               PuntuacionMongoRepository puntuacionMongoRepo) {
        this.participacionMongoRepo = participacionMongoRepo;
        this.puntuacionMongoRepo = puntuacionMongoRepo;
    }

    public Map<String, Object> obtenerHistorial(Long usuarioId, int page, int size) {
        List<ParticipacionDocument> todas = participacionMongoRepo.findByUsuarioId(usuarioId);
        int total = todas.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int from = page * size;
        int to = Math.min(from + size, total);

        List<ParticipacionDocument> pagina = todas.subList(from, to);
        List<Map<String, Object>> torneos = new ArrayList<>();

        for (ParticipacionDocument p : pagina) {
            List<PuntuacionDocument> puntuaciones = puntuacionMongoRepo
                    .findByTorneoIdAndUsuarioId(p.getTorneoId(), usuarioId);

            Map<String, Object> torneo = new LinkedHashMap<>();
            torneo.put("idTorneo", p.getTorneoId());
            torneo.put("nombreTorneo", p.getNombreTorneo());
            torneo.put("puntajeFinal", p.getPuntajeFinal());
            torneo.put("posicionFinal", p.getPosicionFinal());
            torneo.put("rondas", puntuaciones.stream().map(pt -> {
                Map<String, Object> ronda = new LinkedHashMap<>();
                ronda.put("numeroRonda", pt.getNumeroRonda());
                ronda.put("puntajeRonda", pt.getPuntajeTotal());
                ronda.put("flechas", pt.getFlechas().stream().map(f -> {
                    Map<String, Object> flecha = new LinkedHashMap<>();
                    flecha.put("puntaje", f);
                    return flecha;
                }).toList());
                return ronda;
            }).toList());

            torneos.add(torneo);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("torneos", torneos);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", total);
        response.put("totalPages", totalPages);
        return response;
    }

    public Map<String, Object> obtenerEstadisticas(Long usuarioId) {
        List<PuntuacionDocument> puntuaciones = puntuacionMongoRepo.findByUsuarioId(usuarioId);
        int totalFlechas = puntuaciones.stream().mapToInt(p -> p.getFlechas().size()).sum();
        int flechasAcertadas = puntuaciones.stream()
                .flatMapToInt(p -> p.getFlechas().stream().mapToInt(Integer::intValue))
                .filter(p -> p > 0).toArray().length;
        int totalPuntos = puntuaciones.stream().mapToInt(PuntuacionDocument::getPuntajeTotal).sum();
        double promedio = totalFlechas > 0 ? (double) totalPuntos / totalFlechas : 0;
        int porcentajeAcierto = totalFlechas > 0 ? (int) Math.round((flechasAcertadas * 100.0) / totalFlechas) : 0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("torneosTotales", (int) participacionMongoRepo.findByUsuarioId(usuarioId).stream()
                .map(ParticipacionDocument::getTorneoId).distinct().count());
        stats.put("totalFlechas", totalFlechas);
        stats.put("flechasAcertadas", flechasAcertadas);
        stats.put("porcentajeAcierto", porcentajeAcierto);
        stats.put("totalPuntos", totalPuntos);
        stats.put("promedioPuntos", Math.round(promedio * 100.0) / 100.0);
        return stats;
    }
}