import api from "./api";

const arquerosService = {
  obtenerHistorial: async (idUsuario, page = 0, size = 5) => {
    const response = await api.get(`/mongo/arqueros/${idUsuario}/historial`, {
      params: { page, size },
    });
    return response.data;
  },

  obtenerEstadisticas: async (idUsuario) => {
    const response = await api.get(`/mongo/arqueros/${idUsuario}/estadisticas`);
    return response.data;
  },

  obtenerMejoresMes: async () => {
    const response = await api.get("/arqueros/rendimiento/ultimo-mes");
    return response.data;
  },
};

export default arquerosService;