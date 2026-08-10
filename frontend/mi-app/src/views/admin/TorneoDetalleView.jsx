import { useCallback, useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import torneoService from "../../api/apiTorneos.js";
import apiParticipaciones from "../../api/apiParticipaciones.js";
import categoriaServiceDistancias from "../../api/apiCategoriasDistancias.js";
import api from "../../api/api.js";
import TorneoCabecera from "../../components/admin/TorneoCabecera.jsx";
import ListaParticipantes from "../../components/admin/ListaParticipantes.jsx";
import FormularioFlechas from "../../components/admin/FormularioFlechas.jsx";
import Podio from "../../components/admin/Podio.jsx";
import Basemap from "../../components/maps/Basemap.jsx";
import PolygonsLayer from "../../components/maps/PolygonsLayer.jsx";
import FitBoundsFromPolygonLayer from "../../components/maps/FitBoundsFromPolygonLayer.jsx";
import LinesLayer from "../../components/maps/LinesLayer.jsx";
import ArcherDianaSelectorLayer from "../../components/maps/ArcherDianaSelectorLayer.jsx";
import {
  normalizeRoundPoint,
  pointToGeoJSONString,
} from "../../utils/posicionesRonda.js";

function requestErrorMessage(error, fallback) {
  const data = error?.response?.data;
  if (typeof data === "string" && data.trim()) return data;
  return data?.message ?? data?.detail ?? data?.error ?? fallback;
}

function normalizeArrowScores(value) {
  if (Array.isArray(value)) return value;
  if (typeof value === "string") {
    try {
      const parsedValue = JSON.parse(value);
      return Array.isArray(parsedValue) ? parsedValue : [];
    } catch {
      return [];
    }
  }
  return [];
}

function normalizeZonasAmbientales(zonas) {
  if (!Array.isArray(zonas)) return [];
  return zonas
    .map((z) => {
      if (typeof z.geomArea === "string") {
        try {
          return JSON.parse(z.geomArea);
        } catch {
          return null;
        }
      }
      return z.geomArea || z.territorio || null;
    })
    .filter(Boolean);
}

export default function TorneoDetalleView() {
  const { idTorneo } = useParams();
  const navigate = useNavigate();

  const [torneo, setTorneo] = useState(null);
  const [inscritos, setInscritos] = useState([]);
  const [rondas, setRondas] = useState([]);
  const [podio, setPodio] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [mostrarMapa, setMostrarMapa] = useState(true);
  const [zonasAmbientales, setZonasAmbientales] = useState([]);

  const [usuarioSel, setUsuarioSel] = useState("");
  const [rondaSel, setRondaSel] = useState("");
  const [flechas, setFlechas] = useState(["", "", "", "", "", ""]);
  const [guardandoPuntaje, setGuardandoPuntaje] = useState(false);
  const [distanciaTiroM, setDistanciaTiroM] = useState(null);
  const [posicionArquero, setPosicionArquero] = useState(null);
  const [posicionDiana, setPosicionDiana] = useState(null);
  const [posicionesRonda, setPosicionesRonda] = useState([]);
  const [posicionesVersion, setPosicionesVersion] = useState(0);
  const [mapMessage, setMapMessage] = useState("");
  const [registroRondaExistente, setRegistroRondaExistente] = useState(false);
  const [errorPuntaje, setErrorPuntaje] = useState("");

  const cargarDatos = useCallback(async () => {
    try {
      setLoading(true);
      const torneoActual = await torneoService.obtenerTorneo(idTorneo);
      if (!torneoActual) {
        navigate("/admin/torneos");
        return;
      }
      setTorneo(torneoActual);

      const [datosCompletos, rondasData, podioData] = await Promise.all([
        apiParticipaciones
          .obtenerDatosCompletosTorneo(idTorneo)
          .catch(() => null),
        api
          .get(`/mongo/rondas/torneo/${idTorneo}`)
          .then((r) => r.data)
          .catch(() => []),
        torneoActual.estado === "FINISHED"
          ? torneoService.obtenerPodio(idTorneo).catch(() => [])
          : Promise.resolve([]),
      ]);

      setInscritos(datosCompletos?.arqueros || []);
      setRondas(rondasData);
      setPodio(podioData);
    } catch (err) {
      setError("Error al cargar los datos del torneo");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [idTorneo, navigate]);

  useEffect(() => {
    void cargarDatos();
  }, [cargarDatos]);

  useEffect(() => {
    api
      .get("/mapas/zonas-ambientales")
      .then((r) => setZonasAmbientales(normalizeZonasAmbientales(r.data || [])))
      .catch(() => setZonasAmbientales([]));
  }, []);

  useEffect(() => {
    let ignore = false;
    if (!torneo?.categoriaDistanciaId) {
      setDistanciaTiroM(null);
      return undefined;
    }
    categoriaServiceDistancias
      .obtenerPorId(torneo.categoriaDistanciaId)
      .then((categoria) => {
        const distancia = Number(categoria?.distanciaTiro);
        if (!ignore)
          setDistanciaTiroM(
            Number.isFinite(distancia) && distancia > 0 ? distancia : null,
          );
      })
      .catch(() => {
        if (!ignore) setDistanciaTiroM(null);
      });
    return () => {
      ignore = true;
    };
  }, [torneo?.categoriaDistanciaId]);

  useEffect(() => {
    let ignore = false;
    if (!rondaSel) return undefined;
    torneoService
      .obtenerPosicionesRonda(idTorneo, rondaSel)
      .then((posiciones) => {
        if (!ignore)
          setPosicionesRonda(Array.isArray(posiciones) ? posiciones : []);
      })
      .catch(() => null);
    return () => {
      ignore = true;
    };
  }, [idTorneo, posicionesVersion, rondaSel, usuarioSel]);

  useEffect(() => {
    let ignore = false;
    if (!usuarioSel || !rondaSel) {
      setFlechas(["", "", "", "", "", ""]);
      setPosicionArquero(null);
      setPosicionDiana(null);
      setRegistroRondaExistente(false);
      return undefined;
    }
    torneoService
      .obtenerPosicionArqueroEnRonda(idTorneo, rondaSel, usuarioSel)
      .then((posicionSeleccionada) => {
        if (ignore) return;
        const flechasRegistradas = normalizeArrowScores(
          posicionSeleccionada?.flechas,
        );
        const existeRegistro = Boolean(
          posicionSeleccionada?.posicion_arquero ??
          posicionSeleccionada?.posicionArquero ??
          flechasRegistradas.length,
        );
        setRegistroRondaExistente(existeRegistro);
        setFlechas(
          flechasRegistradas.length
            ? flechasRegistradas.map((f) => String(f))
            : ["", "", "", "", "", ""],
        );
        setPosicionArquero(
          normalizeRoundPoint(
            posicionSeleccionada?.posicion_arquero ??
            posicionSeleccionada?.posicionArquero,
          ),
        );
        setPosicionDiana(
          normalizeRoundPoint(
            posicionSeleccionada?.posicion_diana ??
            posicionSeleccionada?.posicionDiana,
          ),
        );
      })
      .catch(() => {
        if (!ignore) {
          setFlechas(["", "", "", "", "", ""]);
          setPosicionArquero(null);
          setPosicionDiana(null);
          setRegistroRondaExistente(false);
        }
      });
    return () => {
      ignore = true;
    };
  }, [idTorneo, posicionesVersion, rondaSel, usuarioSel]);

  const handleSeleccionarUsuario = (idUsuario) => {
    setUsuarioSel(idUsuario);
    setPosicionArquero(null);
    setPosicionDiana(null);
    setRegistroRondaExistente(false);
    setMapMessage("");
  };
  const handleCambiarRonda = (numeroRonda) => {
    setRondaSel(numeroRonda);
    setPosicionArquero(null);
    setPosicionDiana(null);
    setPosicionesRonda([]);
    setRegistroRondaExistente(false);
    setMapMessage("");
  };
  const handleCambiarPosiciones = ({ arquero, diana }) => {
    setPosicionArquero(arquero);
    setPosicionDiana(diana);
  };
  const handleReubicarArquero = () => {
    setPosicionArquero(null);
    setPosicionDiana(null);
    setMapMessage("Selecciona nuevamente la posición del arquero sobre la línea de tiro.");
  };
  const handleReubicarDiana = () => {
    setPosicionDiana(null);
    setMapMessage("Elige una nueva dirección para la diana.");
  };

  const ubicacionesListas = Boolean(posicionArquero && posicionDiana);
  const puedeSeleccionarUbicacion =
    Boolean(usuarioSel && rondaSel) &&
    torneo?.estado === "IN_COURSE" &&
    distanciaTiroM !== null;
  const instruccionMapa = !usuarioSel
    ? "Selecciona un participante para ubicarlo en el mapa."
    : !rondaSel
      ? "Selecciona una ronda para registrar sus ubicaciones."
      : distanciaTiroM === null
        ? "La categoría del torneo debe tener una distancia de tiro mayor que cero."
        : !posicionArquero
          ? "Haz clic sobre la línea de tiro para ubicar al arquero."
          : !posicionDiana
            ? `Haz clic para elegir la dirección de la diana a ${distanciaTiroM} m.`
            : "Arquero y diana ubicados.";

  const handleIniciarTorneo = async () => {
    if (window.confirm("¿Iniciar torneo?")) {
      try {
        await torneoService.iniciarTorneo(idTorneo);
        cargarDatos();
      } catch {
        alert("Error al iniciar el torneo");
      }
    }
  };
  const handleFinalizarTorneo = async () => {
    if (window.confirm("¿Finalizar torneo?")) {
      try {
        await torneoService.finalizarTorneo(idTorneo);
        cargarDatos();
      } catch {
        alert("Error al finalizar el torneo");
      }
    }
  };

  const handleGuardarPuntaje = async (e) => {
    e.preventDefault();
    const flechasInt = flechas.map((f) => Number(f));
    if (
      flechas.length !== 6 ||
      flechas.some(
        (f, i) =>
          f === "" ||
          !Number.isInteger(flechasInt[i]) ||
          flechasInt[i] < 0 ||
          flechasInt[i] > 10,
      )
    ) {
      setErrorPuntaje("Completa las seis flechas con valores enteros entre 0 y 10.");
      return;
    }
    if (!ubicacionesListas) {
      setErrorPuntaje("Debes ubicar al arquero y a la diana antes de registrar las flechas.");
      return;
    }
    const ronda = rondas.find((r) => r.numeroRonda === Number(rondaSel));
    if (!ronda) return;

    setErrorPuntaje("");
    setGuardandoPuntaje(true);
    try {
      await torneoService.registrarPuntaje({
        torneoId: idTorneo,
        rondaId: ronda.id,
        usuarioId: Number(usuarioSel),
        flechas: flechasInt,
        posicionArquero: pointToGeoJSONString(posicionArquero),
        posicionDiana: pointToGeoJSONString(posicionDiana),
      });
      setMapMessage("Puntajes y ubicaciones registrados correctamente.");
      setPosicionesVersion((v) => v + 1);
      void cargarDatos();
    } catch (requestError) {
      setErrorPuntaje(
        requestErrorMessage(requestError, "No se pudieron registrar los puntajes."),
      );
    } finally {
      setGuardandoPuntaje(false);
    }
  };

  if (loading)
    return (
      <div className="text-center mt-5">
        <output className="spinner-border" aria-label="Cargando" />
        <p className="mt-3 text-muted">Cargando detalles del torneo...</p>
      </div>
    );
  if (!torneo) return null;

  return (
    <div className="container-fluid py-3">
      {error && <div className="alert alert-danger">{error}</div>}
      <TorneoCabecera
        torneo={torneo}
        onIniciar={handleIniciarTorneo}
        onFinalizar={handleFinalizarTorneo}
        onVolver={() => navigate("/admin/torneos")}
      />
      <div className="mb-3">
        <button
          type="button"
          className="btn btn-outline-dark"
          onClick={() => setMostrarMapa(!mostrarMapa)}
        >
          {mostrarMapa ? "Ocultar Mapa" : "Mostrar Mapa"}
        </button>
      </div>
      {mostrarMapa && (
        <>
          <div style={{ width: "100%", height: "420px" }}>
            <Basemap>
              <PolygonsLayer
                id="zonas-ambientales"
                data={zonasAmbientales}
                fillPaint={{ "fill-color": "#10b981", "fill-opacity": 0.12 }}
                linePaint={{ "line-color": "#059669", "line-width": 1 }}
              />
              <PolygonsLayer data={torneo.zonaCompetenciaGeoJSON} />
              <LinesLayer data={torneo.lineaTiroGeoJSON} />
              <ArcherDianaSelectorLayer
                enabled={puedeSeleccionarUbicacion}
                zone={torneo.zonaCompetenciaGeoJSON}
                shootingLine={torneo.lineaTiroGeoJSON}
                distanceM={distanciaTiroM}
                value={{ arquero: posicionArquero, diana: posicionDiana }}
                registeredPositions={posicionesRonda}
                onChange={handleCambiarPosiciones}
                onMessage={setMapMessage}
              />
              <FitBoundsFromPolygonLayer polygon={torneo.zonaCompetenciaGeoJSON} />
            </Basemap>
          </div>
          <div className={`alert mt-2 mb-3 ${mapMessage ? "alert-info" : "alert-secondary"}`}>
            {mapMessage || instruccionMapa}
            {posicionArquero && (
              <button type="button" className="btn btn-sm btn-outline-dark ms-3" onClick={handleReubicarArquero}>
                Reubicar arquero
              </button>
            )}
            {posicionArquero && posicionDiana && (
              <button type="button" className="btn btn-sm btn-outline-dark ms-2" onClick={handleReubicarDiana}>
                Reubicar diana
              </button>
            )}
          </div>
        </>
      )}
      <div className="row">
        <div className="col-md-5">
          <ListaParticipantes
            inscritos={inscritos}
            usuarioSel={usuarioSel}
            estadoTorneo={torneo.estado}
            onSeleccionarUsuario={handleSeleccionarUsuario}
          />
        </div>
        <div className="col-md-7">
          <FormularioFlechas
            torneo={torneo}
            usuarioSel={usuarioSel}
            rondas={rondas}
            rondaSel={rondaSel}
            setRondaSel={handleCambiarRonda}
            flechas={flechas}
            setFlechas={setFlechas}
            ubicacionesListas={ubicacionesListas}
            registroRondaExistente={registroRondaExistente}
            guardandoPuntaje={guardandoPuntaje}
            errorPuntaje={errorPuntaje}
            onGuardarPuntaje={handleGuardarPuntaje}
          />
        </div>
      </div>
      {torneo.estado === "FINISHED" && podio.length > 0 && (
        <Podio podio={podio} />
      )}
    </div>
  );
}