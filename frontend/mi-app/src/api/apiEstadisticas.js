import api from "./api";

const estadisticasService = {
  obtenerCorrelacionAmbiental: async () => {
    const response = await api.get("/estadisticas/correlacion-ambiental");
    return response.data;
  },

  obtenerRendimientoMongo: async () => {
    const response = await api.get("/mongo/pipeline/rendimiento");
    return Array.isArray(response.data) ? response.data[0] || {} : {};
  },
};

export default estadisticasService;