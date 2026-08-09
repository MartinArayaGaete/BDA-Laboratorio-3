import React, { useState, useEffect, useCallback } from "react";
import categoriaService from "../../api/apiCategoriasDistancias.js";
import TablaCategoriasDistancias from "../../components/admin/TablaCategoriasDistancias.jsx";
import FormCategoriasDistancias from "../../components/admin/FormCategoriasDistancias.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";

export default function CategoriasDistanciaView() {
  const [categorias, setCategorias] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const cargarCategorias = useCallback(() => {
    setLoading(true);
    categoriaService
      .obtenerTodas()
      .then((data) => {
        setCategorias(data);
        setError("");
      })
      .catch(() => setError("Error al cargar categorías"))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    cargarCategorias();
  }, [cargarCategorias]);

  if (loading) return <LoadingSpinner message="Cargando categorías..." />;

  return (
    <div>
      <h2 className="mb-4" color="black">
        Gestión de categorías de distancia
      </h2>

      {error && <div className="alert alert-danger">{error}</div>}

      <FormCategoriasDistancias onCategoriaCreada={cargarCategorias} />

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
            <TablaCategoriasDistancias
              categorias={categorias}
              onCategoriaEliminada={cargarCategorias}
            />
          )}
        </div>
      </div>
    </div>
  );
}
