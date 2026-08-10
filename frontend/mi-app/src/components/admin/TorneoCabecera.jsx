import React from "react";

export default function TorneoCabecera({
  torneo,
  onIniciar,
  onFinalizar,
  onVolver,
}) {
  if (!torneo) return null;

  const getBadgeClass = (estado) => {
    switch (estado) {
      // MongoDB: FINISHED en lugar de COMPLETED
      case "FINISHED":
        return "bg-success";
      case "IN_COURSE":
        return "bg-primary";
      // MongoDB: PENDIENTE en lugar de NOT_STARTED
      case "PENDIENTE":
        return "bg-secondary";
      default:
        return "bg-dark";
    }
  };

  const getEstadoTexto = (estado) => {
    switch (estado) {
      case "PENDIENTE": return "No Iniciado";
      case "IN_COURSE": return "En Curso";
      case "FINISHED": return "Finalizado";
      default: return estado;
    }
  };

  return (
    <div className="card mb-4">
      <div className="card-body d-flex justify-content-between align-items-center">
        <div>
          {/* MongoDB: nombre en lugar de nombreTorneo */}
          <h3 className="mb-1">{torneo.nombre}</h3>
          {/* MongoDB: estado en lugar de estadoTorneo */}
          <span className={`badge ${getBadgeClass(torneo.estado)}`}>
            {getEstadoTexto(torneo.estado)}
          </span>
        </div>
        <div className="d-flex gap-2">
          {/* MongoDB: PENDIENTE en lugar de NOT_STARTED */}
          {torneo.estado === "PENDIENTE" && (
            <button className="btn btn-warning" onClick={onIniciar}>
              ▶ Iniciar Torneo
            </button>
          )}
          {torneo.estado === "IN_COURSE" && (
            <button className="btn btn-success" onClick={onFinalizar}>
              🏁 Finalizar Torneo
            </button>
          )}
          <button className="btn btn-outline-dark" onClick={onVolver}>
            ← Volver
          </button>
        </div>
      </div>
    </div>
  );
}