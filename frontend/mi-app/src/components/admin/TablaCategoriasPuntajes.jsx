import React, { useState } from "react";
import categoriaServicePuntajes from "../../api/apiCategoriasPuntajes.js";

export default function TablaCategoriasPuntajes({
  categorias,
  onCategoriaActualizada,
}) {
  const [eliminando, setEliminando] = useState(null);
  const [editando, setEditando] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    nombreCategoriaDiana: "",
    puntajeMinimo: "",
  });
  const [guardando, setGuardando] = useState(false);

  const abrirEditar = (categoria) => {
    setEditando(categoria);
    setFormData({
      nombreCategoriaDiana: categoria.nombreCategoriaDiana,
      puntajeMinimo: categoria.puntajeMinimo ?? "",
    });
    setShowModal(true);
  };

  const cerrarModal = () => {
    setShowModal(false);
    setEditando(null);
  };

  const handleGuardar = async () => {
    if (!formData.nombreCategoriaDiana.trim()) {
      alert("El nombre de la categoría es obligatorio");
      return;
    }

    const puntajeMinimoNumerico =
      formData.puntajeMinimo === "" || formData.puntajeMinimo === null
        ? null
        : Number(formData.puntajeMinimo);

    setGuardando(true);
    try {
      await categoriaServicePuntajes.actualizarCategoria(editando.idCategoriaDiana, {
        nombreCategoriaDiana: formData.nombreCategoriaDiana.trim(),
        puntajeMinimo: puntajeMinimoNumerico,
      });
      cerrarModal();
      if (onCategoriaActualizada) onCategoriaActualizada();
    } catch (err) {
      alert("Error al actualizar la categoría");
    } finally {
      setGuardando(false);
    }
  };

  const handleEliminar = async (idCategoriaDiana, nombreCategoriaDiana) => {
    if (!window.confirm(`¿Eliminar la categoría "${nombreCategoriaDiana}"?`)) return;
    setEliminando(idCategoriaDiana);
    try {
      await categoriaServicePuntajes.eliminarCategoria(idCategoriaDiana);
      if (onCategoriaActualizada) onCategoriaActualizada();
    } catch (err) {
      alert("Error al eliminar la categoría");
    } finally {
      setEliminando(null);
    }
  };

  if (!categorias || categorias.length === 0) return null;

  return (
    <>
      <div className="table-responsive">
        <table className="table table-hover align-middle">
          <thead className="table-dark">
            <tr>
              <th className="text-white" style={{ width: "80px" }}>
                ID
              </th>
              <th className="text-white">Nombre</th>
              <th className="text-white">Puntaje Mínimo</th>
              <th className="text-white" style={{ width: "150px" }}>
                Acciones
              </th>
            </tr>
          </thead>
          <tbody>
            {categorias.map((categoria) => (
              <tr key={categoria.idCategoriaDiana}>
                <td className="text-muted">{categoria.idCategoriaDiana}</td>
                <td className="fw-bold">{categoria.nombreCategoriaDiana}</td>
                <td className="text-muted">
                  {categoria.puntajeMinimo == null
                    ? "—"
                    : `${categoria.puntajeMinimo}`}
                </td>
                <td>
                  <button
                    className="btn btn-outline-dark btn-sm me-1"
                    onClick={() => abrirEditar(categoria)}
                  >
                    Editar
                  </button>
                  <button
                    className="btn btn-outline-danger btn-sm"
                    onClick={() =>
                      handleEliminar(
                        categoria.idCategoriaDiana,
                        categoria.nombreCategoriaDiana,
                      )
                    }
                    disabled={eliminando === categoria.idCategoriaDiana}
                  >
                    {eliminando === categoria.idCategoriaDiana ? "..." : "Eliminar"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div
          className="modal d-block"
          tabIndex="-1"
          style={{ backgroundColor: "rgba(0,0,0,0.5)" }}
        >
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header bg-dark text-white">
                <h5 className="modal-title">Editar Categoría</h5>
                <button
                  type="button"
                  className="btn-close btn-close-white"
                  onClick={cerrarModal}
                ></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label fw-bold text-dark">Nombre</label>
                  <input
                    type="text"
                    className="form-control"
                    value={formData.nombreCategoriaDiana}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        nombreCategoriaDiana: e.target.value,
                      })
                    }
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label fw-bold text-dark">
                    Puntaje Mínimo
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    min="0"
                    step="1"
                    value={formData.puntajeMinimo}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        puntajeMinimo: e.target.value,
                      })
                    }
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-outline-dark" onClick={cerrarModal}>
                  Cancelar
                </button>
                <button
                  className="btn btn-dark"
                  onClick={handleGuardar}
                  disabled={guardando}
                >
                  {guardando ? "Guardando..." : "Guardar Cambios"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
