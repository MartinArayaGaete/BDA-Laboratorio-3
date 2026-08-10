// ============================================================
// Poblacion reproducible de MongoDB para el Laboratorio 3.
//
// Contrato de IDs compartido con database/sql/11-testData.sql:
//   usuarios 1..13, categorias 1..4, categorias diana 1..3,
//   torneos 1..5 y rondas 1..15.
//
// Los documentos de este fixture son autoritativos: volver a ejecutar este
// archivo restaura solamente los documentos marcados con SEED_SOURCE y los
// IDs reservados por este fixture. Nunca elimina documentos ajenos al seed.
// ============================================================

const archeryDb = db.getSiblingDB("archerydb");
const SEED_SOURCE = "laboratorio-3-v1";
const referenceDate = new Date();
const seedCalendarTimeZone = "America/Santiago";

function asLong(value) {
  return NumberLong(String(value));
}

function asInt(value) {
  return NumberInt(value);
}

function dateFromToday(days, hour = 12, minute = 0) {
  // PostgreSQL usa CURRENT_DATE con PGTZ=America/Santiago. Convertir primero
  // al mismo dia calendario evita un desfase nocturno con el contenedor UTC.
  const dateParts = {};
  new Intl.DateTimeFormat("en", {
    timeZone: seedCalendarTimeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit"
  }).formatToParts(referenceDate).forEach((part) => {
    if (part.type !== "literal") dateParts[part.type] = Number(part.value);
  });
  const value = new Date(Date.UTC(
    dateParts.year,
    dateParts.month - 1,
    dateParts.day + days,
    hour,
    minute,
    0,
    0
  ));
  return ISODate(value.toISOString());
}

function plusMinutes(value, minutes) {
  return ISODate(new Date(value.getTime() + minutes * 60 * 1000).toISOString());
}

function assertSeed(condition, message) {
  if (!condition) {
    throw new Error(`[seed:${SEED_SOURCE}] ${message}`);
  }
}

const archerNames = {
  2: "Ana Torres",
  3: "Bruno Silva",
  4: "Camila Rojas",
  5: "Diego Muñoz",
  6: "Elena Soto",
  7: "Felipe Vargas",
  8: "Gabriela Pérez",
  9: "Hugo Castillo",
  10: "Isidora Morales",
  11: "Javier Contreras",
  12: "Karla Fuentes",
  13: "Lucas Navarro"
};

const distanceCategories = {
  1: { nombre: "Recurvo Escuela", distanciaTiro: 18 },
  2: { nombre: "Recurvo Indoor", distanciaTiro: 30 },
  3: { nombre: "Compuesto", distanciaTiro: 50 },
  4: { nombre: "Recurvo Olímpico", distanciaTiro: 70 }
};

const targetCategories = {
  1: { nombre: "Diana completa", puntajeMinimo: 0 },
  2: { nombre: "Zona puntuable intermedia", puntajeMinimo: 5 },
  3: { nombre: "Zona puntuable avanzada", puntajeMinimo: 7 }
};

const competitionArea = JSON.stringify({
  type: "Polygon",
  coordinates: [[
    [-70.6500, -33.4500],
    [-70.6460, -33.4500],
    [-70.6460, -33.4460],
    [-70.6500, -33.4460],
    [-70.6500, -33.4500]
  ]]
});

function shootingLineFor(tournamentId) {
  const latitude = Number((-33.44965 + (tournamentId - 1) * 0.00003).toFixed(5));
  return JSON.stringify({
    type: "LineString",
    coordinates: [
      [-70.6497, latitude],
      [-70.6482, latitude]
    ]
  });
}

const tournamentSpecs = [
  {
    id: 1,
    nombre: "Copa Santiago Histórica",
    estado: "FINISHED",
    startOffset: -28,
    endOffset: -26,
    categoryId: 1,
    targetCategoryId: 1,
    plazasMax: 16,
    participantIds: [2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
  },
  {
    id: 2,
    nombre: "Campeonato Metropolitano",
    estado: "FINISHED",
    startOffset: -16,
    endOffset: -14,
    categoryId: 2,
    targetCategoryId: 1,
    plazasMax: 18,
    participantIds: [2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
  },
  {
    id: 3,
    nombre: "Liga de Invierno",
    estado: "IN_COURSE",
    startOffset: -1,
    endOffset: 1,
    categoryId: 3,
    targetCategoryId: 2,
    plazasMax: 12,
    participantIds: [2, 3, 4, 5, 6, 7]
  },
  {
    id: 4,
    nombre: "Torneo Primavera",
    estado: "PENDIENTE",
    startOffset: 10,
    endOffset: 12,
    categoryId: 4,
    targetCategoryId: 3,
    plazasMax: 12,
    participantIds: [8, 9, 10]
  },
  {
    id: 5,
    nombre: "Copa de los Andes",
    estado: "PENDIENTE",
    startOffset: 24,
    endOffset: 26,
    categoryId: 2,
    targetCategoryId: 2,
    plazasMax: 10,
    participantIds: [11, 12]
  }
];

const tournamentsById = {};
tournamentSpecs.forEach((tournament) => {
  tournamentsById[tournament.id] = tournament;
});

function roundState(tournamentId, roundNumber) {
  if (tournamentId === 1 || tournamentId === 2) return "FINISHED";
  if (tournamentId === 3 && roundNumber < 3) return "FINISHED";
  if (tournamentId === 3 && roundNumber === 3) return "IN_COURSE";
  return "PENDIENTE";
}

const roundSpecs = [];
tournamentSpecs.forEach((tournament) => {
  for (let roundNumber = 1; roundNumber <= 3; roundNumber += 1) {
    const id = (tournament.id - 1) * 3 + roundNumber;
    const isCurrentRound = tournament.id === 3 && roundNumber === 3;
    roundSpecs.push({
      id,
      tournamentId: tournament.id,
      roundNumber,
      state: roundState(tournament.id, roundNumber),
      // Ronda 1: calma; ronda 2: lluvia; ronda 3: viento.
      zoneId: [3, 2, 1][roundNumber - 1],
      // La tercera ronda de la liga comenzo hace dos horas y sigue activa.
      date: isCurrentRound
        ? ISODate(new Date(referenceDate.getTime() - 2 * 60 * 60 * 1000).toISOString())
        : dateFromToday(tournament.startOffset + roundNumber - 1, 9)
    });
  }
});

const roundsById = {};
roundSpecs.forEach((round) => {
  roundsById[round.id] = round;
});

function arrowsFor(userId, roundId) {
  const environmentalPenalty = (roundId - 1) % 3;
  const arrows = [];

  for (let arrowNumber = 1; arrowNumber <= 6; arrowNumber += 1) {
    const score = Math.max(
      0,
      Math.min(
        10,
        12
          - Math.floor((userId - 2) / 2)
          - ((userId + roundId + arrowNumber) % 4)
          - environmentalPenalty
      )
    );
    arrows.push(score);
  }

  return arrows;
}

function roundPosition(tournamentId, roundId, userId) {
  const categoryId = tournamentsById[tournamentId].categoryId;
  const targetOffsetByCategory = {
    1: 0.00013,
    2: 0.00022,
    3: 0.00040,
    4: 0.00058
  };
  const archerLongitude = -70.64955 + (userId - 2) * 0.00010;
  const archerLatitude = -33.44955
    + ((roundId - 1) % 3) * 0.00035
    + (tournamentId - 1) * 0.00003;
  const targetLatitude = archerLatitude + targetOffsetByCategory[categoryId];
  const rounded = (number) => Number(number.toFixed(6));

  return {
    archer: JSON.stringify({
      type: "Point",
      coordinates: [rounded(archerLongitude), rounded(archerLatitude)]
    }),
    target: JSON.stringify({
      type: "Point",
      coordinates: [rounded(archerLongitude), rounded(targetLatitude)]
    })
  };
}

const scoreRecords = [];
tournamentSpecs
  .filter((tournament) => tournament.id <= 3)
  .forEach((tournament) => {
    roundSpecs
      .filter((round) => round.tournamentId === tournament.id)
      .forEach((round) => {
        tournament.participantIds.forEach((userId) => {
          const arrows = arrowsFor(userId, round.id);
          const positions = roundPosition(tournament.id, round.id, userId);
          scoreRecords.push({
            tournamentId: tournament.id,
            roundId: round.id,
            roundNumber: round.roundNumber,
            userId,
            arrows,
            total: arrows.reduce((sum, score) => sum + score, 0),
            archerPosition: positions.archer,
            targetPosition: positions.target,
            createdAt: plusMinutes(round.date, userId * 3)
          });
        });
      });
  });

const totalsByTournamentAndUser = {};
scoreRecords.forEach((score) => {
  const key = `${score.tournamentId}:${score.userId}`;
  totalsByTournamentAndUser[key] = (totalsByTournamentAndUser[key] || 0) + score.total;
});

function densePositionsFor(tournamentId) {
  const tournament = tournamentsById[tournamentId];
  const ranked = tournament.participantIds
    .filter((userId) => totalsByTournamentAndUser[`${tournamentId}:${userId}`] !== undefined)
    .map((userId) => ({
      userId,
      total: totalsByTournamentAndUser[`${tournamentId}:${userId}`]
    }))
    .sort((left, right) => right.total - left.total || left.userId - right.userId);

  let lastTotal = null;
  let densePosition = 0;
  const result = {};
  ranked.forEach((entry) => {
    if (entry.total !== lastTotal) {
      densePosition += 1;
      lastTotal = entry.total;
    }
    result[entry.userId] = densePosition;
  });
  return result;
}

const positionsByTournament = {
  1: densePositionsFor(1),
  2: densePositionsFor(2),
  3: densePositionsFor(3)
};

const tournamentDocuments = tournamentSpecs.map((tournament) => {
  const category = distanceCategories[tournament.categoryId];
  const targetCategory = targetCategories[tournament.targetCategoryId];
  return {
    _id: String(tournament.id),
    sqlIdTorneo: asLong(tournament.id),
    nombre: tournament.nombre,
    estado: tournament.estado,
    fechaInicio: dateFromToday(tournament.startOffset),
    fechaTermino: dateFromToday(tournament.endOffset),
    plazasMax: asInt(tournament.plazasMax),
    plazasActual: asInt(tournament.participantIds.length),
    categoriaDistanciaId: asLong(tournament.categoryId),
    categoriaDistancia: {
      nombre: category.nombre,
      distanciaTiro: asInt(category.distanciaTiro)
    },
    categoriaDianaId: asLong(tournament.targetCategoryId),
    categoriaDiana: {
      nombre: targetCategory.nombre,
      puntajeMinimo: asInt(targetCategory.puntajeMinimo)
    },
    zonaCompetenciaGeoJSON: competitionArea,
    lineaTiroGeoJSON: shootingLineFor(tournament.id),
    seedSource: SEED_SOURCE
  };
});

const roundDocuments = roundSpecs.map((round) => {
  const tournament = tournamentsById[round.tournamentId];
  const document = {
    _id: String(round.id),
    torneoId: String(round.tournamentId),
    numeroRonda: asInt(round.roundNumber),
    estado: round.state,
    postgisZonaId: asLong(round.zoneId),
    createdAt: dateFromToday(Math.min(tournament.startOffset - 7, -3), 10),
    seedSource: SEED_SOURCE
  };

  // El modelo no asigna fecha de inicio hasta que la ronda deja PENDIENTE.
  if (round.state !== "PENDIENTE") {
    document.fechaInicio = round.date;
  }
  return document;
});

const participationDocuments = [];
tournamentSpecs.forEach((tournament) => {
  tournament.participantIds.forEach((userId, participantIndex) => {
    const total = totalsByTournamentAndUser[`${tournament.id}:${userId}`] || 0;
    const document = {
      _id: `participacion-${tournament.id}-${userId}`,
      torneoId: String(tournament.id),
      usuarioId: asLong(userId),
      nombreArquero: archerNames[userId],
      nombreTorneo: tournament.nombre,
      puntajeFinal: asInt(total),
      inscritoEn: dateFromToday(
        Math.min(tournament.startOffset - 7, -3),
        10,
        participantIndex * 2
      ),
      seedSource: SEED_SOURCE
    };

    if (tournament.estado === "FINISHED") {
      document.posicionFinal = asInt(positionsByTournament[tournament.id][userId]);
    }
    participationDocuments.push(document);
  });
});

const scoreDocuments = scoreRecords.map((score) => {
  const tournament = tournamentsById[score.tournamentId];
  const targetCategory = targetCategories[tournament.targetCategoryId];
  return {
    _id: `puntuacion-${score.tournamentId}-${score.roundId}-${score.userId}`,
    torneoId: String(score.tournamentId),
    rondaId: String(score.roundId),
    usuarioId: asLong(score.userId),
    nombreArquero: archerNames[score.userId],
    nombreTorneo: tournament.nombre,
    numeroRonda: asInt(score.roundNumber),
    categoria: distanceCategories[tournament.categoryId].nombre,
    puntajeMinimo: asInt(targetCategory.puntajeMinimo),
    flechas: score.arrows.map(asInt),
    puntajeTotal: asInt(score.total),
    posicionArquero: score.archerPosition,
    posicionDiana: score.targetPosition,
    createdAt: score.createdAt,
    updatedAt: plusMinutes(score.createdAt, 5),
    seedSource: SEED_SOURCE
  };
});

const rankingDocuments = tournamentsById[3].participantIds.map((userId) => ({
  _id: `ranking-3-${userId}`,
  torneoId: "3",
  usuarioId: asLong(userId),
  nombreArquero: archerNames[userId],
  nombreTorneo: tournamentsById[3].nombre,
  puntajeTotal: asInt(totalsByTournamentAndUser[`3:${userId}`]),
  posicion: asInt(positionsByTournament[3][userId]),
  ultimaActualizacion: plusMinutes(roundsById[9].date, 60),
  seedSource: SEED_SOURCE
}));

function replaceFixtureDocuments(collectionName, documents) {
  const collection = archeryDb.getCollection(collectionName);
  const currentIds = documents.map((document) => document._id);

  // Solo limpia versiones anteriores del mismo fixture; preserva datos reales.
  collection.deleteMany({
    seedSource: SEED_SOURCE,
    _id: { $nin: currentIds }
  });

  if (documents.length > 0) {
    collection.bulkWrite(
      documents.map((document) => ({
        replaceOne: {
          filter: { _id: document._id },
          replacement: document,
          upsert: true
        }
      })),
      { ordered: true }
    );
  }

  const persisted = collection.countDocuments({ seedSource: SEED_SOURCE });
  assertSeed(
    persisted === documents.length,
    `${collectionName}: se esperaban ${documents.length} fixtures y se encontraron ${persisted}`
  );
  print(`${collectionName}: ${persisted} fixtures verificados`);
}

// Comprobaciones previas: fallar antes de modificar Mongo si el manifiesto
// coordinado entre PostgreSQL y MongoDB deja de ser consistente.
assertSeed(tournamentDocuments.length === 5, "deben existir 5 torneos");
assertSeed(roundDocuments.length === 15, "deben existir 15 rondas");
assertSeed(participationDocuments.length === 35, "deben existir 35 participaciones");
assertSeed(scoreDocuments.length === 90, "deben existir 90 puntuaciones");
assertSeed(
  scoreRecords.reduce((count, score) => count + score.arrows.length, 0) === 540,
  "deben existir exactamente 540 flechas"
);
assertSeed(rankingDocuments.length === 6, "el ranking vivo debe tener 6 arqueros");

const scoreNaturalKeys = new Set();
scoreRecords.forEach((score) => {
  const naturalKey = `${score.tournamentId}:${score.roundId}:${score.userId}`;
  assertSeed(!scoreNaturalKeys.has(naturalKey), `puntuacion duplicada ${naturalKey}`);
  scoreNaturalKeys.add(naturalKey);

  const tournament = tournamentsById[score.tournamentId];
  const round = roundsById[score.roundId];
  const minimum = targetCategories[tournament.targetCategoryId].puntajeMinimo;
  assertSeed(round.tournamentId === score.tournamentId, `ronda huerfana ${score.roundId}`);
  assertSeed(
    tournament.participantIds.includes(score.userId),
    `usuario ${score.userId} no inscrito en torneo ${score.tournamentId}`
  );
  assertSeed(score.arrows.length === 6, `${naturalKey} no contiene 6 flechas`);
  assertSeed(
    score.arrows.every((arrow) => arrow === 0 || (arrow >= minimum && arrow <= 10)),
    `${naturalKey} incumple el minimo de diana ${minimum}`
  );
  assertSeed(
    score.arrows.reduce((sum, arrow) => sum + arrow, 0) === score.total,
    `${naturalKey} tiene un total inconsistente`
  );
});

tournamentSpecs.forEach((tournament) => {
  assertSeed(
    tournament.participantIds.length <= tournament.plazasMax,
    `${tournament.nombre} excede sus plazas`
  );
});

replaceFixtureDocuments("torneos", tournamentDocuments);
replaceFixtureDocuments("rondas", roundDocuments);
replaceFixtureDocuments("participaciones", participationDocuments);
replaceFixtureDocuments("puntuaciones", scoreDocuments);
replaceFixtureDocuments("ranking_vivo", rankingDocuments);

const duplicatedScores = archeryDb.puntuaciones.aggregate([
  { $match: { seedSource: SEED_SOURCE } },
  {
    $group: {
      _id: { torneoId: "$torneoId", rondaId: "$rondaId", usuarioId: "$usuarioId" },
      cantidad: { $sum: 1 }
    }
  },
  { $match: { cantidad: { $gt: 1 } } },
  { $limit: 1 }
]).toArray();
assertSeed(duplicatedScores.length === 0, "hay puntuaciones duplicadas en MongoDB");

const persistedArrowCount = archeryDb.puntuaciones.aggregate([
  { $match: { seedSource: SEED_SOURCE } },
  { $project: { cantidad: { $size: "$flechas" } } },
  { $group: { _id: null, total: { $sum: "$cantidad" } } }
]).toArray();
assertSeed(
  persistedArrowCount.length === 1 && Number(persistedArrowCount[0].total) === 540,
  "el recuento persistido debe ser de 540 flechas"
);

print(`Seed ${SEED_SOURCE} completado: 5 torneos, 15 rondas, 35 participaciones, 90 puntuaciones y 540 flechas.`);
