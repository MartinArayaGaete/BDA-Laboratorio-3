import React, { useState, useEffect, useCallback } from "react";
import categoriaServicePuntajes from "../../api/apiCategoriasPuntajes.js";
import TablaCategoriasPuntajes from "../../components/admin/TablaCategoriasPuntajes.jsx";
import FormCategoriasPuntajes from "../../components/admin/FormCategoriasPuntajes.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";

export default function CategoriasPuntajesView() {
  const [categorias, setCategorias] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const cargarCategoriasDiana = useCallback(() => {
    setLoading(true);
    categoriaServicePuntajes
      .obtenerTodas()
      .then((data) => {
        setCategorias(data);
        setError("");
      })
      .catch(() => setError("Error al cargar categorías"))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    cargarCategoriasDiana();
  }, [cargarCategoriasDiana]);

  if (loading) return <LoadingSpinner message="Cargando categorías..." />;

  return (
    <div>
      <h2 className="mb-4" color="black">
        Gestión de categorías de diana
      </h2>

      {error && <div className="alert alert-danger">{error}</div>}

      <FormCategoriasPuntajes onCategoriaCreada={cargarCategoriasDiana} />

      <div className="card">
        <div className="card-header d-flex justify-content-between align-items-center">
          <strong>Categorías registradas</strong>
          <span className="badge bg-secondary">{categorias.length}</span>
        </div>
        <div className="card-body">
          {categorias.length === 0 ? (
            <p className="text-muted text-center mb-0">
              No hay categorías registradas
            </p>
          ) : (
            <TablaCategoriasPuntajes
              categorias={categorias}
              onCategoriaEliminada={cargarCategoriasDiana}
            />
          )}
        </div>
      </div>
    </div>
  );
}
