const archeryDb = db.getSiblingDB("archerydb");

const pipeline = [
  {
    $project: {
      torneoId: 1,
      nombreTorneo: 1,
      categoria: 1,
      usuarioId: 1,
      nombreArquero: 1,
      numeroRonda: 1,
      puntajeTotal: 1
    }
  },
  {
    $group: {
      _id: {
        torneoId: "$torneoId",
        categoria: "$categoria",
        usuarioId: "$usuarioId"
      },
      nombreTorneo: { $first: "$nombreTorneo" },
      nombreArquero: { $first: "$nombreArquero" },
      promedioPuntaje: { $avg: "$puntajeTotal" }, 
      mejorRonda: {
        $top: {
          sortBy: { puntajeTotal: -1, numeroRonda: 1 },
          output: {
            numeroRonda: "$numeroRonda",
            puntaje: "$puntajeTotal"
          }
        }
      },
      rondasRegistradas: { $sum: 1 }
    }
  },
  {
    $project: {
      _id: 0,
      torneoId: "$_id.torneoId",
      nombreTorneo: 1,
      categoria: "$_id.categoria",
      usuarioId: "$_id.usuarioId",
      nombreArquero: 1,
      promedioPuntaje: 1,
      mejorRonda: 1,
      rondasRegistradas: 1
    }
  },
  {
    $facet: {
      detallePorArquero: [
        {
          $sort: {
            torneoId: 1,
            categoria: 1,
            promedioPuntaje: -1,
            "mejorRonda.puntaje": -1,
            usuarioId: 1
          }
        }
      ],
      distribucionPorRendimiento: [
        {
          $bucket: {
            groupBy: "$promedioPuntaje",
            boundaries: [0, 15, 30, 45, 61],
            default: "fuera_de_rango",
            output: {
              cantidadDesempenos: { $sum: 1 },
              arqueros: {
                $push: {
                  torneoId: "$torneoId",
                  nombreTorneo: "$nombreTorneo",
                  categoria: "$categoria",
                  usuarioId: "$usuarioId",
                  nombreArquero: "$nombreArquero",
                  promedioPuntaje: "$promedioPuntaje",
                  mejorRonda: "$mejorRonda",
                  rondasRegistradas: "$rondasRegistradas"
                }
              }
            }
          }
        }
      ]
    }
  }
];

printjson(archeryDb.puntuaciones.aggregate(pipeline).toArray());