import api from "./api";

const apiRondas = {
  obtenerTodas: async () => {
    const response = await api.get("/mongo/rondas");
    return response.data;
  },

  obtenerPorTorneo: async (idTorneo) => {
    const response = await api.get(`/mongo/rondas/torneo/${idTorneo}`);
    return response.data;
  },

  crearRonda: async (rondaData) => {
    const response = await api.post("/mongo/rondas", rondaData);
    return response.data;
  },

  verPuntajeRonda: async (idParticipacion, idRonda) => {
    const response = await api.get(
      `/mongo/puntuaciones/ronda/${idRonda}`,
    );
    return response.data;
  },

  asignarZonaAmbiental: async (idRonda, idZonaAmbiental) => {
    const response = await api.put(`/mongo/rondas/${idRonda}/zona-ambiental`, {
      idZonaAmbiental,
    });
    return response.data;
  },

  eliminarRonda: async (idRonda) => {
    const response = await api.delete(`/mongo/rondas/${idRonda}`);
    return response.data;
  },
};

export default apiRondas;