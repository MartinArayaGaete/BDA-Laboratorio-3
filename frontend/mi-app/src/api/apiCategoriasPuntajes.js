import api from "./api";

const categoriaServicePuntajes = {
  obtenerTodas: async () => {
    const response = await api.get("/categorias-diana");
    return response.data;
  },

  obtenerPorId: async (idCategoria) => {
    const response = await api.get(`/categorias-diana/${idCategoria}`);
    return response.data;
  },

  crearCategoria: async (categoria) => {
    const response = await api.post("/categorias-diana", categoria);
    return response.data;
  },

  actualizarCategoria: async (idCategoria, categoria) => {
    const response = await api.put(`/categorias-diana/${idCategoria}`, categoria);
    return response.data;
  },

  eliminarCategoria: async (idCategoria) => {
    const response = await api.delete(`/categorias-diana/${idCategoria}`);
    return response.data;
  },
};

export default categoriaServicePuntajes;
