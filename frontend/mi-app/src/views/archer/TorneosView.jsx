import React, { useState, useEffect, useCallback } from "react";
import { useAuth } from "../../hooks/useAuth";
import torneoService from "../../api/apiTorneos.js";
import api from "../../api/api.js";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";

export default function TorneosView() {
  const { user } = useAuth();
  const [torneos, setTorneos] = useState([]);
  const [inscritos, setInscritos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("disponibles");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [inscribiendo, setInscribiendo] = useState(null);

  const cargarTorneos = useCallback(async () => {
    if (!user?.idUsuario) return;
    try {
      setLoading(true);
      setError("");
      const [todosTorneos, torneosInscritos] = await Promise.all([
        torneoService.obtenerTodos().catch(() => []),
        api.get(`/mongo/participaciones/usuario/${user.idUsuario}`)
          .then((r) => r.data)
          .catch(() => []),
      ]);
      const torneosArray = Array.isArray(todosTorneos) ? todosTorneos : [];
      const inscritosArray = Array.isArray(torneosInscritos)
        ? torneosInscritos
        : [];

      // MongoDB: PENDIENTE, id, torneoId
      const disponibles = torneosArray.filter(
        (t) =>
          (t.estado === "PENDIENTE" || t.estadoTorneo === "NOT_STARTED") &&
          !inscritosArray.some(
            (i) => (i.torneoId || i.id_torneo) === (t.id || t.idTorneo)
          ),
      );
      setTorneos(disponibles);
      setInscritos(inscritosArray);
    } catch (err) {
      setError("Error al cargar torneos");
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    cargarTorneos();
  }, [cargarTorneos]);

  const handleInscribir = async (torneo) => {
    setError("");
    setSuccess("");
    const idTorneo = torneo.id || torneo.idTorneo;
    setInscribiendo(idTorneo);

    try {
      await api.post("/mongo/participaciones", {
        torneoId: idTorneo,
        usuarioId: user.idUsuario,
      });
      setSuccess("¡Inscripción exitosa!");
      cargarTorneos();
      setTimeout(() => setSuccess(""), 3000);
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.response?.data ||
        "Error al inscribirse";
      setError(typeof msg === "string" ? msg : "Error al inscribirse");
    } finally {
      setInscribiendo(null);
    }
  };

  const getEstadoBadge = (estado) => {
    const estados = {
      PENDIENTE: { class: "bg-secondary", text: "Próximo" },
      NOT_STARTED: { class: "bg-secondary", text: "Próximo" },
      IN_COURSE: { class: "bg-primary", text: "En Curso" },
      FINISHED: { class: "bg-success", text: "Finalizado" },
      COMPLETED: { class: "bg-success", text: "Finalizado" },
    };
    const config = estados[estado] || { class: "bg-dark", text: estado || "?" };
    return <span className={`badge ${config.class}`}>{config.text}</span>;
  };

  if (loading) return <LoadingSpinner message="Cargando torneos..." />;

  return (
    <div>
      <h2 className="mb-4 text-dark">Mis Torneos</h2>

      {error && <div className="alert alert-danger alert-dismissible fade show">{error}<button className="btn-close" onClick={() => setError("")}></button></div>}
      {success && <div className="alert alert-success alert-dismissible fade show">{success}<button className="btn-close" onClick={() => setSuccess("")}></button></div>}

      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button className={`nav-link ${activeTab === "disponibles" ? "active" : ""}`} onClick={() => setActiveTab("disponibles")}>
            Disponibles ({torneos.length})
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${activeTab === "inscritos" ? "active" : ""}`} onClick={() => setActiveTab("inscritos")}>
            Inscritos ({inscritos.length})
          </button>
        </li>
      </ul>

      {/* DISPONIBLES */}
      {activeTab === "disponibles" && (torneos.length === 0 ? (
        <div className="text-center py-5 text-muted"><p>No hay torneos disponibles</p></div>
      ) : (
        <div className="row">
          {torneos.map((torneo) => (
            <div key={torneo.id || torneo.idTorneo} className="col-md-6 mb-3">
              <div className="card shadow-sm h-100">
                <div className="card-body">
                  <h5 className="card-title text-dark">{torneo.nombre || torneo.nombreTorneo}</h5>
                  <p className="text-muted small">{torneo.fechaInicio} → {torneo.fechaTermino}</p>
                  <p className="text-muted small">
                    Plazas: {torneo.plazasActual ?? torneo.nroPlazaActual ?? 0}/{torneo.plazasMax ?? torneo.nroPlazaMax ?? "?"}
                  </p>
                  {getEstadoBadge(torneo.estado || torneo.estadoTorneo)}
                  <button
                    className="btn btn-dark btn-sm mt-2 w-100"
                    onClick={() => handleInscribir(torneo)}
                    disabled={
                      inscribiendo === (torneo.id || torneo.idTorneo) ||
                      (torneo.plazasActual ?? torneo.nroPlazaActual ?? 0) >= (torneo.plazasMax ?? torneo.nroPlazaMax ?? 999)
                    }
                  >
                    {inscribiendo === (torneo.id || torneo.idTorneo) ? "Inscribiendo..." :
                     (torneo.plazasActual ?? torneo.nroPlazaActual ?? 0) >= (torneo.plazasMax ?? torneo.nroPlazaMax ?? 999) ? "Sin plazas" : "Inscribirme"}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      ))}

      {/* INSCRITOS */}
      {activeTab === "inscritos" && (inscritos.length === 0 ? (
        <div className="text-center py-5 text-muted"><p>No estás inscrito en ningún torneo</p></div>
      ) : (
        <div className="row">
          {inscritos.map((torneo) => (
            <div key={torneo.id || torneo.torneoId || torneo.idTorneo || torneo.id_torneo} className="col-md-6 mb-3">
              <div className="card shadow-sm h-100">
                <div className="card-body">
                  <h5 className="card-title text-dark">
                    {torneo.nombreTorneo || torneo.nombre_torneo || torneo.nombre || "Torneo"}
                  </h5>
                  <p className="text-muted small">
                    {torneo.fechaInicio || torneo.fecha_inicio || "?"} → {torneo.fechaTermino || torneo.fecha_termino || "?"}
                  </p>
                  {getEstadoBadge(torneo.estado || torneo.estadoTorneo || torneo.estado_torneo || "IN_COURSE")}
                </div>
              </div>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}