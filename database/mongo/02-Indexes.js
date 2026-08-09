const archeryDb = db.getSiblingDB("archerydb");
const puntuaciones = archeryDb.getCollection("puntuaciones");

const indexName = puntuaciones.createIndex(
  { torneoId: 1, rondaId: 1, usuarioId: 1 },
  {
    name: "uk_puntuacion_torneo_ronda_usuario",
    unique: true
  }
);

print(`si vez esto es porque el indice esta asegurado y activo: ${indexName}`);

const sample = puntuaciones.findOne(
    {},
    { _id: 0, torneoId: 1, rondaId: 1, usuarioId: 1 }
);

const scoringFilter = sample || {
    torneoId: "torneo-de-ejemplo",
    rondaId: "ronda-de-ejemplo",
    usuarioId: NumberLong("0")
};

print("ejecutando simulacion de consulta para auditar el rendimiento del indice:");

printjson(
    puntuaciones
        .find(scoringFilter)
        .explain("executionStats")
);