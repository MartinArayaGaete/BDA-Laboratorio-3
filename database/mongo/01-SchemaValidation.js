const archeryDb = db.getSiblingDB("archerydb");

function applyValidator(collectionName, jsonSchema, dynamicEvaluation = null) {
  const validator = dynamicEvaluation ? 
  {$and: [{ $jsonSchema: jsonSchema },dynamicEvaluation] } : { $jsonSchema: jsonSchema};

  const validatorOptions = {
    validator: validator,
    validationLevel: "strict",
    validationAction: "error"
  };

  if (archeryDb.getCollectionInfos({ name: collectionName }).length === 0) {
    archeryDb.createCollection(collectionName, validatorOptions);
  } else {
    const result = archeryDb.runCommand({
      collMod: collectionName,
      ...validatorOptions
    });

    if (!result.ok) {
      throw new Error(result.errmsg || `Could not update ${collectionName}`);
    }
  }

  print(`Schema validation applied to ${collectionName}`);
}

applyValidator("puntuaciones", {
    bsonType: "object",
    required: ["torneoId", "rondaId", "usuarioId", "puntajeMinimo", "flechas", "puntajeTotal"],
    properties: {
      torneoId: {
        bsonType: "string",
        minLength: 1
      },
      rondaId: {
        bsonType: "string",
        minLength: 1
      },
      usuarioId: {
        bsonType: "long"
      },
      nombreArquero: {
        bsonType: "string"
      },
      nombreTorneo: {
        bsonType: "string"
      },
      numeroRonda: {
        bsonType: "int"
      },
      categoria: {
        bsonType: "string"
      },
      puntajeMinimo: {
        bsonType: "int",
        minimum: 0
      },
      flechas: {
        bsonType: "array",
        minItems: 1,
        maxItems: 6,
        items: {
          bsonType: "int",
        }
      },
      puntajeTotal: {
        bsonType: "int",
        minimum: 0,
        maximum: 60
      },
      posicionArquero: {
        bsonType: "string"
      },
      posicionDiana: {
        bsonType: "string"
      },
      createdAt: {
        bsonType: "date"
      },
      updatedAt: {
        bsonType: "date"
      }
    }
  },
  {
    $expr: {
      $allElementsTrue: [
        {
          $map: {
            input: "$flechas",
            as: "flecha",
            in: {
              $or: [
                {
                  $eq: ["$$flecha", 0]
                },
                {
                  $and: [
                    { $gte: ["$$flecha", "$puntajeMinimo"] },
                    { $lte: ["$$flecha", 10] }
                  ]
                }
              ]
            }
          }
        }
      ]
    }
  }
);

applyValidator("torneos", {
  bsonType: "object",
  required: ["nombre", "estado", "plazasMax", "plazasActual"],
  properties: {
    nombre: {
      bsonType: "string",
      minLength: 1
    },
    sqlIdTorneo: {
      bsonType: "long"
    },
    estado: {
      bsonType: "string",
      enum: ["PENDIENTE", "IN_COURSE", "FINISHED"]
    },
    fechaInicio: {
      bsonType: "date"
    },
    fechaTermino: {
      bsonType: "date"
    },
    plazasMax: {
      bsonType: "int",
      minimum: 0
    },
    plazasActual: {
      bsonType: "int",
      minimum: 0
    },
    categoriaDistanciaId: {
      bsonType: "long"
    },
    categoriaDistancia: {
      bsonType: "object",
      properties: {
        nombre: {
          bsonType: "string"
        },
        distanciaTiro: {
          bsonType: "int",
          minimum: 0
        }
      }
    },
    categoriaDianaId: {
      bsonType: "long"
    },
    categoriaDiana: {
      bsonType: "object",
      properties: {
        nombre: {
          bsonType: "string"
        },
        puntajeMinimo: {
          bsonType: "int"
        }
      }
    },
    zonasAmbientales: {
      bsonType: "array",
      items: {
        bsonType: "object",
        properties: {
          idZonaAmbiental: {
            bsonType: "long"
          },
          idCategoriaAmbiental: {
            bsonType: "long"
          },
          categoriaAmbiental: {
            bsonType: "string"
          }
        }
      }
    }
  }
});

applyValidator("rondas", {
  bsonType: "object",
  required: ["torneoId", "numeroRonda", "estado"],
  properties: {
    torneoId: {
      bsonType: "string",
      minLength: 1
    },
    numeroRonda: {
      bsonType: "int"
    },
    estado: {
      bsonType: "string",
      enum: ["PENDIENTE", "IN_COURSE", "FINISHED"]
    },
    postgisZonaId: {
      bsonType: "long"
    },
    fechaInicio: {
      bsonType: "date"
    },
    createdAt: {
      bsonType: "date"
    }
  }
});

applyValidator("participaciones", {
  bsonType: "object",
  required: ["torneoId", "usuarioId", "puntajeFinal"],
  properties: {
    torneoId: {
      bsonType: "string",
      minLength: 1
    },
    usuarioId: {
      bsonType: "long"
    },
    nombreArquero: {
      bsonType: "string"
    },
    nombreTorneo: {
      bsonType: "string"
    },
    puntajeFinal: {
      bsonType: "int",
      minimum: 0
    },
    posicionFinal: {
      bsonType: "int",
      minimum: 1
    },
    inscritoEn: {
      bsonType: "date"
    }
  }
});

applyValidator("ranking_vivo", {
  bsonType: "object",
  required: [
    "torneoId",
    "usuarioId",
    "nombreArquero",
    "nombreTorneo",
    "puntajeTotal",
    "posicion",
    "ultimaActualizacion"
  ],
  properties: {
    torneoId: {
      bsonType: "string",
      minLength: 1
    },
    usuarioId: {
      bsonType: "long"
    },
    nombreArquero: {
      bsonType: "string",
      minLength: 1
    },
    nombreTorneo: {
      bsonType: "string",
      minLength: 1
    },
    puntajeTotal: {
      bsonType: "int",
      minimum: 0
    },
    posicion: {
      bsonType: "int",
      minimum: 1
    },
    ultimaActualizacion: {
      bsonType: "date"
    }
  }
});