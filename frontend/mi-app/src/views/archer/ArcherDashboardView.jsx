import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../api/api";

function ArcherDashboard() {
  const PAGE_SIZE = 6;
  const navigate = useNavigate();

  const [usuario, setUsuario] = useState(null);
  const [usuarioCargado, setUsuarioCargado] = useState(false);
  const [torneos, setTorneos] = useState([]);
  const [torneosCargando, setTorneosCargando] = useState(true);
  const [torneosPage, setTorneosPage] = useState(0);
  const [torneosTotalPages, setTorneosTotalPages] = useState(0);
  const [historial, setHistorial] = useState([]);
  const [historialCargando, setHistorialCargando] = useState(true);
  const [historialPage, setHistorialPage] = useState(0);
  const [historialTotalPages, setHistorialTotalPages] = useState(0);
  const [flechasPorTorneo, setFlechasPorTorneo] = useState({});
  const [estadisticas, setEstadisticas] = useState({
    torneosTotales: 0,
    totalFlechas: 0,
    flechasAcertadas: 0,
    porcentajeAcierto: 0,
    totalPuntos: 0,
    promedioPuntos: 0,
  });
  const [estadisticasCargando, setEstadisticasCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState("");
  const [inscribiendoTorneo, setInscribiendoTorneo] = useState(null);
  const [errorInscripcion, setErrorInscripcion] = useState("");
  const [desinscribiendoTorneo, setDesinscribiendoTorneo] = useState(null);
  const [errorDesinscripcion, setErrorDesinscripcion] = useState("");

  const esErrorAuth = (err) => {
    const status = err?.response?.status;
    return status === 401 || status === 403;
  };

  const cargarTorneos = async (pagina = torneosPage) => {
    try {
      setTorneosCargando(true);
      setErrorCarga("");
      const resp = await api.get("/mongo/torneos");
      const todos = Array.isArray(resp.data) ? resp.data : [];
      const disponibles = todos.filter(
        (t) => t.estado !== "FINISHED" && t.plazasActual < t.plazasMax,
      );
      const total = disponibles.length;
      const totalPages = Math.ceil(total / PAGE_SIZE);
      const inicio = pagina * PAGE_SIZE;
      const fin = inicio + PAGE_SIZE;
      const paginaActual = disponibles.slice(inicio, fin);
      setTorneos(paginaActual);
      setTorneosTotalPages(totalPages);
    } catch (err) {
      if (esErrorAuth(err)) {
        localStorage.removeItem("usuarioLogueado");
        navigate("/login");
        return;
      }
      setErrorCarga("No se pudieron cargar los torneos.");
      setTorneos([]);
      setTorneosTotalPages(0);
    } finally {
      setTorneosCargando(false);
    }
  };

  const cargarHistorial = async (pagina = historialPage) => {
    try {
      setHistorialCargando(true);
      setErrorCarga("");
      const resp = await api.get(
        `/mongo/arqueros/${usuario.idUsuario}/historial`,
        { params: { page: pagina, size: PAGE_SIZE } },
      );
      const payload = resp.data || {};
      const torneosPagina = Array.isArray(payload.torneos)
        ? payload.torneos
        : [];
      const totalPages = Number(payload.totalPages || 0);
      const flechasPorTorneoLocal = {};
      torneosPagina.forEach((torneo) => {
        const rondas = Array.isArray(torneo.rondas) ? torneo.rondas : [];
        const flechasDelTorneo = [];
        rondas.forEach((ronda) => {
          const numeroRonda = ronda?.numeroRonda;
          const flechasRonda = Array.isArray(ronda?.flechas)
            ? ronda.flechas
            : [];
          flechasRonda.forEach((f, index) => {
            // El backend devuelve {puntaje: 10}
            const puntaje = typeof f === "object" ? f.puntaje : f;
            flechasDelTorneo.push({
              idFlecha: index + 1,
              puntaje,
              numeroRonda,
            });
          });
        });
        flechasPorTorneoLocal[torneo.idTorneo] = flechasDelTorneo;
      });
      setHistorial(torneosPagina);
      setHistorialTotalPages(totalPages);
      setFlechasPorTorneo(flechasPorTorneoLocal);
    } catch (err) {
      if (esErrorAuth(err)) {
        localStorage.removeItem("usuarioLogueado");
        navigate("/login");
        return;
      }
      setErrorCarga("No se pudo cargar el historial.");
      setHistorial([]);
      setHistorialTotalPages(0);
      setFlechasPorTorneo({});
    } finally {
      setHistorialCargando(false);
    }
  };

  const cargarEstadisticas = async () => {
    try {
      setEstadisticasCargando(true);
      const resp = await api.get(
        `/mongo/arqueros/${usuario.idUsuario}/estadisticas`,
      );
      setEstadisticas(resp.data || {});
    } catch (err) {
      if (esErrorAuth(err)) {
        localStorage.removeItem("usuarioLogueado");
        navigate("/login");
        return;
      }
      setEstadisticas({
        torneosTotales: 0,
        totalFlechas: 0,
        flechasAcertadas: 0,
        porcentajeAcierto: 0,
        totalPuntos: 0,
        promedioPuntos: 0,
      });
    } finally {
      setEstadisticasCargando(false);
    }
  };

  useEffect(() => {
    const usuarioGuardado = localStorage.getItem("usuarioLogueado");
    if (usuarioGuardado) {
      try {
        setUsuario(JSON.parse(usuarioGuardado));
      } catch {
        localStorage.removeItem("usuarioLogueado");
        navigate("/login");
      }
    } else {
      navigate("/login");
    }
    setUsuarioCargado(true);
  }, [navigate]);

  useEffect(() => {
    if (!usuario) return;
    cargarTorneos();
  }, [usuario, torneosPage]);
  useEffect(() => {
    if (!usuario) return;
    cargarHistorial();
  }, [usuario, historialPage]);
  useEffect(() => {
    if (!usuario) return;
    cargarEstadisticas();
  }, [usuario]);

  const handleInscribirse = async (torneo) => {
    if (!usuario) return;
    if (torneo.estado !== "PENDIENTE") {
      setErrorInscripcion(
        "El torneo debe estar en estado PENDIENTE para inscribirse.",
      );
      setTimeout(() => setErrorInscripcion(""), 3000);
      return;
    }
    setInscribiendoTorneo(torneo.id);
    setErrorInscripcion("");
    try {
      await api.post("/mongo/participaciones", {
        torneoId: torneo.id,
        usuarioId: usuario.idUsuario,
      });
      await Promise.all([
        cargarTorneos(torneosPage),
        cargarHistorial(historialPage),
        cargarEstadisticas(),
      ]);
      setInscribiendoTorneo(null);
    } catch (err) {
      setErrorInscripcion(
        String(
          err?.response?.data?.error ||
            err?.response?.data?.message ||
            "Error al inscribirse.",
        ),
      );
      setTimeout(() => setErrorInscripcion(""), 4000);
      setInscribiendoTorneo(null);
    }
  };

  const handleDesinscribirse = async (torneo) => {
    if (!usuario) return;
    if (
      torneo.estado !== "PENDIENTE" &&
      torneo.estadoTorneo !== "NOT_STARTED"
    ) {
      setErrorDesinscripcion(
        "No puedes desinscribirte de un torneo que ya inició.",
      );
      setTimeout(() => setErrorDesinscripcion(""), 3000);
      return;
    }
    if ((flechasPorTorneo[torneo.idTorneo] || []).length > 0) {
      setErrorDesinscripcion(
        "No puedes desinscribirte: ya tienes flechas registradas.",
      );
      setTimeout(() => setErrorDesinscripcion(""), 3000);
      return;
    }
    const nombreTorneo = torneo.nombreTorneo || torneo.nombre || "";
    if (!window.confirm(`¿Desinscribirte de "${nombreTorneo}"?`)) return;
    const idTorneo = torneo.idTorneo || torneo.id;
    setDesinscribiendoTorneo(idTorneo);
    setErrorDesinscripcion("");
    try {
      await api.delete(
        `/mongo/participaciones/${idTorneo}/${usuario.idUsuario}`,
      );
      await Promise.all([
        cargarTorneos(torneosPage),
        cargarHistorial(historialPage),
        cargarEstadisticas(),
      ]);
      setDesinscribiendoTorneo(null);
    } catch (err) {
      setErrorDesinscripcion(
        String(
          err?.response?.data?.error ||
            err?.response?.data?.message ||
            "Error al desinscribirse.",
        ),
      );
      setTimeout(() => setErrorDesinscripcion(""), 4000);
      setDesinscribiendoTorneo(null);
    }
  };

  const torneosArray = Array.isArray(torneos) ? torneos : [];
  const historialArray = Array.isArray(historial) ? historial : [];
  const historialPaginado = historialArray;
  const puntosMaximos = Math.max(
    ...historialArray.map((item) => item.puntajeFinal || 0),
    1,
  );
  const stats = estadisticas || {};

  if (!usuarioCargado)
    return (
      <div className="container-fluid py-4 px-4">
        <div className="alert alert-dark">Cargando usuario...</div>
      </div>
    );

  return (
    <div className="container-fluid py-4 px-4">
      <h1 className="text-dark">Mi Perfil de Arquero</h1>
      {usuario && (
        <p className="text-dark">
          {usuario.nombre} (ID: {usuario.idUsuario})
        </p>
      )}
      {errorCarga && <div className="alert alert-danger">{errorCarga}</div>}

      <section className="mb-5">
        <div className="row g-3 mb-4">
          {[
            { l: "Torneos", v: stats.torneosTotales },
            { l: "Total Flechas", v: stats.totalFlechas },
            { l: "% Acierto", v: stats.porcentajeAcierto + "%" },
            { l: "Promedio Puntos", v: stats.promedioPuntos },
          ].map((c, i) => (
            <div className="col-md-3" key={i}>
              <div className="card text-white bg-dark p-3 text-center">
                <h6>{c.l}</h6>
                <h2>{c.v}</h2>
              </div>
            </div>
          ))}
        </div>
        <div className="row g-3">
          <div className="col-md-5">
            <div className="card p-3 h-100 bg-white">
              <h6 className="mb-3 text-dark">Aciertos vs Fallos</h6>
              {(() => {
                const a = stats.flechasAcertadas,
                  f = Math.max(stats.totalFlechas - a, 0),
                  t = a + f;
                if (!t)
                  return (
                    <div
                      className="d-flex align-items-center justify-content-center bg-light rounded-circle border mx-auto"
                      style={{ width: 180, height: 180 }}
                    >
                      <span className="text-dark">Sin datos</span>
                    </div>
                  );
                const p = (a / t) * 100;
                return (
                  <div
                    className="position-relative mx-auto"
                    style={{ width: 180, height: 180 }}
                  >
                    <div
                      className="rounded-circle w-100 h-100"
                      style={{
                        background: `conic-gradient(#333 0 ${p}%, #999 ${p}% 100%)`,
                      }}
                    />
                    <div
                      className="position-absolute top-50 start-50 translate-middle text-center bg-white rounded-circle d-flex flex-column justify-content-center align-items-center border border-dark"
                      style={{ width: 110, height: 110 }}
                    >
                      <strong className="fs-4 text-dark">
                        {stats.porcentajeAcierto}%
                      </strong>
                      <small className="text-dark">acierto</small>
                    </div>
                  </div>
                );
              })()}
              <div className="d-flex justify-content-center gap-3 mt-3 small">
                <span className="text-dark">
                  <span
                    className="badge me-1"
                    style={{ backgroundColor: "#333" }}
                  >
                    &nbsp;
                  </span>
                  Acertadas: {stats.flechasAcertadas}
                </span>
                <span className="text-dark">
                  <span
                    className="badge me-1"
                    style={{ backgroundColor: "#999" }}
                  >
                    &nbsp;
                  </span>
                  Fallidas:{" "}
                  {Math.max(stats.totalFlechas - stats.flechasAcertadas, 0)}
                </span>
              </div>
            </div>
          </div>
          <div className="col-md-7">
            <div className="card p-3 h-100 bg-white">
              <h6 className="mb-3 text-dark">Puntos por Torneo</h6>
              {historialArray.length === 0 ? (
                <div className="alert alert-dark border mb-0">
                  Sin torneos en el historial.
                </div>
              ) : (
                <div className="d-flex flex-column gap-3">
                  {historialArray.map((item) => {
                    const v = item.puntajeFinal || 0,
                      w = `${Math.max((v / puntosMaximos) * 100, 6)}%`;
                    return (
                      <div key={item.idTorneo}>
                        <div className="d-flex justify-content-between small mb-1">
                          <span className="fw-medium text-dark">
                            {item.nombreTorneo}
                          </span>
                          <span className="text-dark">{v} pts</span>
                        </div>
                        <div className="progress" style={{ height: 14 }}>
                          <div
                            className="progress-bar bg-dark"
                            style={{ width: w }}
                          />
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </div>
      </section>

      <section className="mb-5">
        <h5 className="mb-3 text-dark">Mi Historial</h5>
        {errorDesinscripcion && (
          <div className="alert alert-danger alert-dismissible fade show">
            {errorDesinscripcion}
            <button
              className="btn-close"
              onClick={() => setErrorDesinscripcion("")}
            />
          </div>
        )}
        {historialCargando ? (
          <div className="alert alert-dark">Cargando...</div>
        ) : historial.length === 0 ? (
          <div className="alert alert-dark">
            No hay torneos registrados aún.
          </div>
        ) : (
          <>
            <div className="row">
              {historialPaginado.map((item) => (
                <div className="col-md-6 mb-3" key={item.idTorneo}>
                  <div className="card p-3 shadow-sm bg-white">
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <div>
                        <h6 className="mb-1 text-dark">{item.nombreTorneo}</h6>
                        <span className="badge bg-dark text-white">
                          {item.estado || item.estadoTorneo || "PENDIENTE"}
                        </span>
                      </div>
                      {(item.estado === "PENDIENTE" ||
                        item.estadoTorneo === "NOT_STARTED" ||
                        !item.estado) &&
                        (flechasPorTorneo[item.idTorneo] || []).length ===
                          0 && (
                          <button
                            className="btn btn-sm btn-outline-danger ms-2"
                            onClick={() => handleDesinscribirse(item)}
                            disabled={desinscribiendoTorneo === item.idTorneo}
                          >
                            {desinscribiendoTorneo === item.idTorneo
                              ? "..."
                              : "✕"}
                          </button>
                        )}
                    </div>
                    <p className="mb-1 text-dark">
                      Puntaje: <strong>{item.puntajeFinal}</strong>
                    </p>
                    <p className="mb-0 text-dark">
                      Posición: <strong>{item.posicionFinal ?? "-"}°</strong>
                    </p>
                    <details className="mt-2">
                      <summary
                        className="small text-dark"
                        style={{ cursor: "pointer" }}
                      >
                        Ver flechas (
                        {(flechasPorTorneo[item.idTorneo] || []).length})
                      </summary>
                      <div className="mt-2 small">
                        {(() => {
                          const ff = flechasPorTorneo[item.idTorneo] || [];
                          if (!ff.length)
                            return (
                              <div className="text-dark">
                                Sin flechas registradas
                              </div>
                            );
                          const ag = ff.reduce((a, f) => {
                            const r = f.numeroRonda || 0;
                            if (!a[r]) a[r] = [];
                            a[r].push(f);
                            return a;
                          }, {});
                          return Object.keys(ag)
                            .sort((a, b) => Number(a) - Number(b))
                            .map((r) => (
                              <div key={r} className="mb-2">
                                <strong className="text-dark">Ronda {r}</strong>
                                <ul className="mb-0">
                                  {ag[r].map((f) => (
                                    <li key={f.idFlecha} className="text-dark">
                                      Flecha {f.idFlecha}: {f.puntaje}
                                    </li>
                                  ))}
                                </ul>
                              </div>
                            ));
                        })()}
                      </div>
                    </details>
                  </div>
                </div>
              ))}
            </div>
            {historialTotalPages > 1 && (
              <div className="mt-2">
                {Array.from({ length: historialTotalPages }).map((_, i) => (
                  <button
                    key={i}
                    className={`btn btn-sm me-1 ${i === historialPage ? "btn-dark" : "btn-outline-dark"}`}
                    onClick={() => setHistorialPage(i)}
                  >
                    {i + 1}
                  </button>
                ))}
              </div>
            )}
          </>
        )}
      </section>

      <section className="mb-5">
        <h5 className="mb-3 text-dark">Torneos Disponibles</h5>
        {errorInscripcion && (
          <div className="alert alert-danger alert-dismissible fade show">
            {errorInscripcion}
            <button
              className="btn-close"
              onClick={() => setErrorInscripcion("")}
            />
          </div>
        )}
        {torneosCargando ? (
          <div className="alert alert-dark">Cargando torneos...</div>
        ) : torneosArray.length === 0 ? (
          <div className="alert alert-dark">No hay torneos disponibles.</div>
        ) : (
          <>
            <div className="row">
              {torneosArray.map((t) => (
                <div className="col-md-6 mb-3" key={t.id || t.idTorneo}>
                  <div className="card p-3 bg-white">
                    <div className="d-flex justify-content-between align-items-center">
                      <div>
                        <h6 className="mb-0 text-dark">
                          {t.nombre || t.nombreTorneo}
                        </h6>
                        <small className="text-dark">
                          {t.fechaInicio} — {t.fechaTermino}
                        </small>
                        <div className="mt-1">
                          <span className="badge bg-dark text-white">
                            {t.estado || t.estadoTorneo}
                          </span>
                          <small className="ms-2 text-dark">
                            Plazas: {t.plazasActual ?? t.nroPlazaActual ?? 0}/
                            {t.plazasMax ?? t.nroPlazaMax ?? "?"}
                          </small>
                        </div>
                      </div>
                      {(t.estado === "PENDIENTE" ||
                        t.estadoTorneo === "NOT_STARTED") && (
                        <button
                          className="btn btn-sm btn-dark"
                          onClick={() => handleInscribirse(t)}
                          disabled={
                            inscribiendoTorneo === (t.id || t.idTorneo) ||
                            (t.plazasActual ?? t.nroPlazaActual ?? 0) >=
                              (t.plazasMax ?? t.nroPlazaMax ?? 999)
                          }
                        >
                          {inscribiendoTorneo === (t.id || t.idTorneo)
                            ? "..."
                            : (t.plazasActual ?? t.nroPlazaActual ?? 0) >=
                                (t.plazasMax ?? t.nroPlazaMax ?? 999)
                              ? "Sin plazas"
                              : "Inscribirme"}
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
            {torneosTotalPages > 1 && (
              <div className="mt-2">
                {Array.from({ length: torneosTotalPages }).map((_, i) => (
                  <button
                    key={i}
                    className={`btn btn-sm me-1 ${i === torneosPage ? "btn-dark" : "btn-outline-dark"}`}
                    onClick={() => setTorneosPage(i)}
                  >
                    {i + 1}
                  </button>
                ))}
              </div>
            )}
          </>
        )}
      </section>

      <section className="mb-5">
        <h5 className="mb-3 text-dark">Mis Flechas</h5>
        <div className="alert alert-dark">
          Total de flechas cargadas: {stats.totalFlechas}
        </div>
      </section>
    </div>
  );
}

export default ArcherDashboard;
