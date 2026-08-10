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
    $facet: {
      rendimientoPorTorneo: [
        {
          $group: {
            _id: {
              torneoId: "$torneoId",
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
            usuarioId: "$_id.usuarioId",
            nombreTorneo: 1,
            nombreArquero: 1,
            promedioPuntaje: 1,
            mejorRonda: 1,
            rondasRegistradas: 1
          }
        },
        {
          $sort: {
            torneoId: 1,
            promedioPuntaje: -1,
            "mejorRonda.puntaje": -1,
            usuarioId: 1
          }
        }
      ],
      rendimientoPorCategoria: [
        {
          $group: {
            _id: {
              categoria: "$categoria",
              usuarioId: "$usuarioId"
            },
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
            rondasConsideradas: { $sum: 1 }
          }
        },
        {
          $project: {
            _id: 0,
            categoria: "$_id.categoria",
            usuarioId: "$_id.usuarioId",
            nombreArquero: 1,
            promedioPuntaje: 1,
            mejorRonda: 1,
            rondasConsideradas: 1
          }
        },
        {
          $sort: {
            categoria: 1,
            promedioPuntaje: -1,
            "mejorRonda.puntaje": -1,
            usuarioId: 1
          }
        }
      ],
      distribucionPorRendimiento: [
        {
          $group: {
            _id: {
              categoria: "$categoria",
              usuarioId: "$usuarioId"
            },
            nombreArquero: { $first: "$nombreArquero" },
            promedioPuntaje: { $avg: "$puntajeTotal" },
            rondasConsideradas: { $sum: 1 }
          }
        },
        {
          $bucket: {
            groupBy: "$promedioPuntaje",
            boundaries: [0, 15, 30, 45, 61],
            default: "fuera_de_rango",
            output: {
              cantidadDesempenos: { $sum: 1 },
              arqueros: {
                $push: {
                  categoria: "$_id.categoria",
                  usuarioId: "$_id.usuarioId",
                  nombreArquero: "$nombreArquero",
                  promedioPuntaje: "$promedioPuntaje",
                  rondasConsideradas: "$rondasConsideradas"
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