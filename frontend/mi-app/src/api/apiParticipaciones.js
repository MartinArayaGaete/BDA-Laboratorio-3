import api from "./api";

const apiParticipaciones = {
  obtenerTodas: async () => {
    try {
      const response = await api.get("/mongo/participaciones");
      return response.data;
    } catch (error) {
      return [];
    }
  },

  obtenerInscritosPorTorneo: async (idTorneo) => {
    try {
      const response = await api.get(`/mongo/participaciones/torneo/${idTorneo}`);
      return response.data || [];
    } catch (error) {
      return [];
    }
  },

  obtenerDatosCompletosTorneo: async (idTorneo) => {
    const response = await api.get(
      `/mongo/participaciones/torneo/${idTorneo}/completo`,
    );
    return response.data;
  },

  inscribirArquero: async (idTorneo, idUsuario) => {
    const response = await api.post("/mongo/participaciones", {
      torneoId: idTorneo,
      usuarioId: idUsuario,
    });
    return response.data;
  },

  desinscribirArquero: async (idTorneo, idUsuario) => {
    const response = await api.delete(
      `/mongo/participaciones/${idTorneo}/${idUsuario}`
    );
    return response.data;
  },

  obtenerTorneosInscritos: async (idUsuario) => {
    try {
      const response = await api.get(`/mongo/participaciones/usuario/${idUsuario}`);
      return response.data || [];
    } catch (error) {
      return [];
    }
  },
};

export default apiParticipaciones;