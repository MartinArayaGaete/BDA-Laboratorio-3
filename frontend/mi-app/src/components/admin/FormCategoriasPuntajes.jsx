import React, { useState } from "react";
import categoriaServicePuntajes from "../../api/apiCategoriasPuntajes.js";

export default function FormCategoriasPuntajes({ onCategoriaCreada }) {
  const [nombreCategoria, setNombreCategoria] = useState("");
  const [puntuaciónMinima, setPuntuaciónMinima] = useState("");
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!nombreCategoria.trim()) {
      setError("El nombre de la categoría es obligatorio");
      return;
    }

    const puntuaciónMinimaNumerica = puntuaciónMinima.trim() === "" ? null : Number(puntuaciónMinima);

    if (puntuaciónMinima.trim() !== "" && (!Number.isInteger(puntuaciónMinimaNumerica) || puntuaciónMinimaNumerica < 0)) {
      setError("La puntuación mínima debe ser un número entero mayor o igual a 0");
      return;
    } else if (puntuaciónMinima.trim() !== "" && (!Number.isInteger(puntuaciónMinimaNumerica) || puntuaciónMinimaNumerica > 10)){
      setError("La puntuación mínima debe ser un número entero menor o igual a 10");
      return;
    }

    setGuardando(true);
    try {
      await categoriaServicePuntajes.crearCategoria({
        nombreCategoria: nombreCategoria.trim(),
        puntuaciónMinima: puntuaciónMinimaNumerica,
      });

      const mensajePuntuacion = puntuaciónMinimaNumerica === null ? "" : ` con puntuación mínima de ${puntuaciónMinimaNumerica}`;
      setSuccess(`Categoría "${nombreCategoria.trim()}" creada exitosamente${mensajePuntuacion}`);
      setNombreCategoria("");
      setPuntuaciónMinima("");
      if (onCategoriaCreada) onCategoriaCreada();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err) {
      setError(err.response?.data?.error || "Error al crear la categoría");
    } finally {
      setGuardando(false);
    }
  };

  return (
    <div className="card mb-4">
      <div className="card-header">
        <strong>Nueva Categoría</strong>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <form onSubmit={handleSubmit} className="row g-2 align-items-end">
          <div className="col-md-6">
            <label className="form-label">Nombre</label>
            <input
              type="text"
              className="form-control"
              placeholder="Ej: General, Avanzado, Profesional, etc."
              value={nombreCategoria}
              onChange={(e) => setNombreCategoria(e.target.value)}
              disabled={guardando}
            />
          </div>
          <div className="col-md-3">
            <label className="form-label">Puntuación mínima</label>
            <input
              type="number"
              className="form-control"
              min="0"
              step="1"
              placeholder="Ej: 0, 1, 2, etc."
              value={puntuaciónMinima}
              onChange={(e) => setPuntuaciónMinima(e.target.value)}
              disabled={guardando}
            />
          </div>
          <div className="col-md-3">
            <button
              type="submit"
              className="btn btn-dark w-100"
              disabled={guardando || !nombreCategoria.trim()}
            >
              {guardando ? "Creando..." : "Crear Categoría"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
