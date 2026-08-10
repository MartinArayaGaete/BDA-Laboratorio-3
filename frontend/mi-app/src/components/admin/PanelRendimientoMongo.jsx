import { useEffect, useMemo, useState } from "react";
import estadisticasService from "../../api/apiEstadisticas.js";
import LoadingSpinner from "../common/LoadingSpinner.jsx";

function formatNumber(value) {
  const numberValue = Number(value);
  return Number.isFinite(numberValue) ? numberValue.toFixed(2) : "0.00";
}

function bestRoundLabel(mejorRonda) {
  if (!mejorRonda) return "Sin datos";
  return `Ronda ${mejorRonda.numeroRonda ?? "-"} (${mejorRonda.puntaje ?? 0} pts)`;
}

function bucketLabel(value) {
  if (value === "fuera_de_rango") return "Fuera de rango";
  const start = Number(value);
  if (!Number.isFinite(start)) return String(value);
  const end = start === 45 ? 60 : start + 14;
  return `${start}-${end} pts`;
}

export default function PanelRendimientoMongo() {
  const [data, setData] = useState({
    rendimientoPorTorneo: [],
    rendimientoPorCategoria: [],
    distribucionPorRendimiento: [],
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false;

    estadisticasService
      .obtenerRendimientoMongo()
      .then((result) => {
        if (ignore) return;
        setData({
          rendimientoPorTorneo: Array.isArray(result.rendimientoPorTorneo)
            ? result.rendimientoPorTorneo
            : [],
          rendimientoPorCategoria: Array.isArray(result.rendimientoPorCategoria)
            ? result.rendimientoPorCategoria
            : [],
          distribucionPorRendimiento: Array.isArray(result.distribucionPorRendimiento)
            ? result.distribucionPorRendimiento
            : [],
        });
      })
      .catch((err) => {
        console.error("Error al cargar rendimiento Mongo:", err);
        if (!ignore) setError("No se pudieron cargar las estadísticas Mongo.");
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, []);

  const resumen = useMemo(() => {
    const arqueros = new Set(
      data.rendimientoPorTorneo.map((item) => String(item.usuarioId)),
    );
    return {
      torneos: new Set(data.rendimientoPorTorneo.map((item) => item.torneoId)).size,
      categorias: new Set(data.rendimientoPorCategoria.map((item) => item.categoria)).size,
      arqueros: arqueros.size,
    };
  }, [data]);

  if (loading) return <LoadingSpinner message="Cargando rendimiento..." />;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (
    <div className="mb-4">
      <div className="row g-3 mb-3">
        <div className="col-md-4">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <span className="text-muted small">Torneos con rendimiento</span>
              <h3 className="mb-0 text-dark">{resumen.torneos}</h3>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <span className="text-muted small">Categorías evaluadas</span>
              <h3 className="mb-0 text-dark">{resumen.categorias}</h3>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <span className="text-muted small">Arqueros considerados</span>
              <h3 className="mb-0 text-dark">{resumen.arqueros}</h3>
            </div>
          </div>
        </div>
      </div>

      <div className="card shadow-sm bg-white mb-3">
        <div className="card-body">
          <h5 className="text-dark mb-3">Rendimiento por Torneo</h5>
          <div className="table-responsive">
            <table className="table table-hover align-middle">
              <thead className="table-dark">
                <tr>
                  <th>Torneo</th>
                  <th>Arquero</th>
                  <th>Promedio</th>
                  <th>Mejor ronda</th>
                  <th>Rondas</th>
                </tr>
              </thead>
              <tbody>
                {data.rendimientoPorTorneo.map((item) => (
                  <tr key={`${item.torneoId}-${item.usuarioId}`}>
                    <td>{item.nombreTorneo}</td>
                    <td>{item.nombreArquero}</td>
                    <td>{formatNumber(item.promedioPuntaje)}</td>
                    <td>{bestRoundLabel(item.mejorRonda)}</td>
                    <td>{item.rondasRegistradas}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="card shadow-sm bg-white mb-3">
        <div className="card-body">
          <h5 className="text-dark mb-3">Rendimiento por Categoría</h5>
          <div className="table-responsive">
            <table className="table table-hover align-middle">
              <thead className="table-dark">
                <tr>
                  <th>Categoría</th>
                  <th>Arquero</th>
                  <th>Promedio</th>
                  <th>Mejor ronda</th>
                  <th>Rondas</th>
                </tr>
              </thead>
              <tbody>
                {data.rendimientoPorCategoria.map((item) => (
                  <tr key={`${item.categoria}-${item.usuarioId}`}>
                    <td>{item.categoria}</td>
                    <td>{item.nombreArquero}</td>
                    <td>{formatNumber(item.promedioPuntaje)}</td>
                    <td>{bestRoundLabel(item.mejorRonda)}</td>
                    <td>{item.rondasConsideradas}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="card shadow-sm bg-white">
        <div className="card-body">
          <h5 className="text-dark mb-3">Distribución por Rendimiento</h5>
          <div className="row g-3">
            {data.distribucionPorRendimiento.map((bucket) => (
              <div key={bucket._id} className="col-md-6 col-xl-3">
                <div className="border rounded p-3 h-100">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <h6 className="mb-0 text-dark">{bucketLabel(bucket._id)}</h6>
                    <span className="badge bg-dark">
                      {bucket.cantidadDesempenos}
                    </span>
                  </div>
                  {(bucket.arqueros || []).map((arquero) => (
                    <div
                      key={`${bucket._id}-${arquero.categoria}-${arquero.usuarioId}`}
                      className="small border-top py-2"
                    >
                      <div className="fw-semibold text-dark">
                        {arquero.nombreArquero}
                      </div>
                      <div className="text-muted">
                        {arquero.categoria} · promedio{" "}
                        {formatNumber(arquero.promedioPuntaje)} ·{" "}
                        {arquero.rondasConsideradas} rondas
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
