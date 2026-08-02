import api from "./api";

const apiRondas = {
  obtenerTodas: async () => {
    const response = await api.get("/rondas");
    return response.data;
  },

  obtenerPorTorneo: async (idTorneo) => {
    const response = await api.get(`/rondas/torneo/${idTorneo}`);
    return response.data;
  },

  crearRonda: async (rondaData) => {
    const response = await api.post("/rondas", rondaData);
    return response.data;
  },

  verPuntajeRonda: async (idParticipacion, idRonda) => {
    const response = await api.get(
      `/rondas/participacion/${idParticipacion}/ronda/${idRonda}/puntaje`,
    );
    return response.data;
  },

  asignarZonaAmbiental: async (idRonda, idZonaAmbiental) => {
    const response = await api.put(`/rondas/${idRonda}/zona-ambiental`, {
      idZonaAmbiental,
    });
    return response.data;
  },

  eliminarRonda: async (idRonda) => {
    const response = await api.delete(`/rondas/${idRonda}`);
    return response.data;
  },
};

export default apiRondas;
