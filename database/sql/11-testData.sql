-- ============================================
-- 11-testData.sql
-- Poblacion determinista de demostracion
--
-- Volumen esperado:
--   1 administrador + 12 arqueros
--   4 categorias de distancia + 3 categorias de diana
--   5 torneos, 15 rondas y 35 participaciones
--   90 puntajes de ronda y 540 flechas
--
-- Los identificadores se fijan explicitamente para que MongoDB pueda
-- reutilizarlos. El script es reejecutable: usuarios y categorias se
-- actualizan por ID, y los escenarios de torneo 1..5 se reconstruyen de forma
-- acotada para no conservar participaciones o puntajes obsoletos del seed.
-- ============================================

BEGIN;

-- Los IDs 1..5 de torneo estan reservados para esta poblacion. Al reejecutar
-- el script se reconstruyen solamente esos escenarios; los torneos creados
-- posteriormente por la aplicacion conservan sus datos.
DELETE FROM logs
WHERE torneo_afectado BETWEEN 1 AND 5;

DELETE FROM torneo
WHERE id_torneo BETWEEN 1 AND 5;

-- ============================================
-- 1. Usuarios
-- ============================================

INSERT INTO usuario (
    id_usuario,
    rut,
    nombre,
    correo,
    contrasena,
    rol
)
OVERRIDING SYSTEM VALUE
VALUES
    (1,  '1111111-1',  'Administrador General', 'admin@archery.cl',              'admin123', 'ADMIN'),
    (2,  '10000001-K', 'Ana Torres',             'ana.torres@archery.cl',         'arco123',  'ARQUERO'),
    (3,  '10000002-8', 'Bruno Silva',            'bruno.silva@archery.cl',        'arco123',  'ARQUERO'),
    (4,  '10000003-6', 'Camila Rojas',           'camila.rojas@archery.cl',       'arco123',  'ARQUERO'),
    (5,  '10000004-4', 'Diego Muñoz',            'diego.munoz@archery.cl',        'arco123',  'ARQUERO'),
    (6,  '10000005-2', 'Elena Soto',             'elena.soto@archery.cl',         'arco123',  'ARQUERO'),
    (7,  '10000006-0', 'Felipe Vargas',          'felipe.vargas@archery.cl',      'arco123',  'ARQUERO'),
    (8,  '10000007-9', 'Gabriela Pérez',         'gabriela.perez@archery.cl',     'arco123',  'ARQUERO'),
    (9,  '10000008-7', 'Hugo Castillo',          'hugo.castillo@archery.cl',      'arco123',  'ARQUERO'),
    (10, '10000009-5', 'Isidora Morales',        'isidora.morales@archery.cl',    'arco123',  'ARQUERO'),
    (11, '10000010-9', 'Javier Contreras',       'javier.contreras@archery.cl',   'arco123',  'ARQUERO'),
    (12, '10000011-7', 'Karla Fuentes',          'karla.fuentes@archery.cl',      'arco123',  'ARQUERO'),
    (13, '10000012-5', 'Lucas Navarro',          'lucas.navarro@archery.cl',      'arco123',  'ARQUERO')
ON CONFLICT (id_usuario) DO UPDATE
SET rut        = EXCLUDED.rut,
    nombre     = EXCLUDED.nombre,
    correo     = EXCLUDED.correo,
    contrasena = EXCLUDED.contrasena,
    rol        = EXCLUDED.rol;

-- ============================================
-- 2. Categorias
-- ============================================

INSERT INTO categoria (
    id_categoria,
    nombre_categoria,
    distancia_tiro
)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Recurvo Escuela', 18),
    (2, 'Recurvo Indoor', 30),
    (3, 'Compuesto', 50),
    (4, 'Recurvo Olímpico', 70)
ON CONFLICT (id_categoria) DO UPDATE
SET nombre_categoria = EXCLUDED.nombre_categoria,
    distancia_tiro   = EXCLUDED.distancia_tiro;

INSERT INTO categoria_diana (
    id_categoria_diana,
    nombre_categoria_diana,
    puntaje_minimo
)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Diana completa', 0),
    (2, 'Zona puntuable intermedia', 5),
    (3, 'Zona puntuable avanzada', 7)
ON CONFLICT (id_categoria_diana) DO UPDATE
SET nombre_categoria_diana = EXCLUDED.nombre_categoria_diana,
    puntaje_minimo         = EXCLUDED.puntaje_minimo;

-- ============================================
-- 3. Torneos
--
-- Los torneos historicos se cargan primero como IN_COURSE porque tanto el
-- procedimiento registrar_puntaje_ronda como el trigger geoespacial exigen
-- ese estado. Se marcan COMPLETED una vez registrados todos sus puntajes.
-- ============================================

INSERT INTO torneo (
    id_torneo,
    id_categoria,
    nombre_torneo,
    estado_torneo,
    fecha_inicio,
    fecha_termino,
    espacio_torneo,
    linea_de_tiro,
    nro_plaza_max,
    nro_plaza_actual
)
OVERRIDING SYSTEM VALUE
VALUES
    (
        1,
        1,
        'Copa Santiago Histórica',
        'IN_COURSE',
        CURRENT_DATE - 28,
        CURRENT_DATE - 26,
        ST_GeomFromText(
            'POLYGON((-70.6500 -33.4500,-70.6460 -33.4500,-70.6460 -33.4460,-70.6500 -33.4460,-70.6500 -33.4500))',
            4326
        ),
        ST_GeomFromText(
            'LINESTRING(-70.6497 -33.44965,-70.6482 -33.44965)',
            4326
        ),
        16,
        0
    ),
    (
        2,
        2,
        'Campeonato Metropolitano',
        'IN_COURSE',
        CURRENT_DATE - 16,
        CURRENT_DATE - 14,
        ST_GeomFromText(
            'POLYGON((-70.6500 -33.4500,-70.6460 -33.4500,-70.6460 -33.4460,-70.6500 -33.4460,-70.6500 -33.4500))',
            4326
        ),
        ST_GeomFromText(
            'LINESTRING(-70.6497 -33.44962,-70.6482 -33.44962)',
            4326
        ),
        18,
        0
    ),
    (
        3,
        3,
        'Liga de Invierno',
        'IN_COURSE',
        CURRENT_DATE - 1,
        CURRENT_DATE + 1,
        ST_GeomFromText(
            'POLYGON((-70.6500 -33.4500,-70.6460 -33.4500,-70.6460 -33.4460,-70.6500 -33.4460,-70.6500 -33.4500))',
            4326
        ),
        ST_GeomFromText(
            'LINESTRING(-70.6497 -33.44959,-70.6482 -33.44959)',
            4326
        ),
        12,
        0
    ),
    (
        4,
        4,
        'Torneo Primavera',
        'NOT_STARTED',
        CURRENT_DATE + 10,
        CURRENT_DATE + 12,
        ST_GeomFromText(
            'POLYGON((-70.6500 -33.4500,-70.6460 -33.4500,-70.6460 -33.4460,-70.6500 -33.4460,-70.6500 -33.4500))',
            4326
        ),
        ST_GeomFromText(
            'LINESTRING(-70.6497 -33.44956,-70.6482 -33.44956)',
            4326
        ),
        12,
        0
    ),
    (
        5,
        2,
        'Copa de los Andes',
        'NOT_STARTED',
        CURRENT_DATE + 24,
        CURRENT_DATE + 26,
        ST_GeomFromText(
            'POLYGON((-70.6500 -33.4500,-70.6460 -33.4500,-70.6460 -33.4460,-70.6500 -33.4460,-70.6500 -33.4500))',
            4326
        ),
        ST_GeomFromText(
            'LINESTRING(-70.6497 -33.44953,-70.6482 -33.44953)',
            4326
        ),
        10,
        0
    )
ON CONFLICT (id_torneo) DO UPDATE
SET id_categoria     = EXCLUDED.id_categoria,
    nombre_torneo    = EXCLUDED.nombre_torneo,
    estado_torneo    = EXCLUDED.estado_torneo,
    fecha_inicio     = EXCLUDED.fecha_inicio,
    fecha_termino    = EXCLUDED.fecha_termino,
    espacio_torneo   = EXCLUDED.espacio_torneo,
    linea_de_tiro    = EXCLUDED.linea_de_tiro,
    nro_plaza_max    = EXCLUDED.nro_plaza_max;

-- ============================================
-- 4. Rondas
--
-- Cada torneo usa la misma secuencia ambiental:
--   ronda 1: calma (zona 3)
--   ronda 2: lluvia ligera (zona 2)
--   ronda 3: viento fuerte (zona 1)
-- ============================================

INSERT INTO ronda (
    id_ronda,
    id_torneo,
    id_zona_ambiental,
    numero_ronda
)
OVERRIDING SYSTEM VALUE
VALUES
    (1,  1, 3, 1),
    (2,  1, 2, 2),
    (3,  1, 1, 3),
    (4,  2, 3, 1),
    (5,  2, 2, 2),
    (6,  2, 1, 3),
    (7,  3, 3, 1),
    (8,  3, 2, 2),
    (9,  3, 1, 3),
    (10, 4, 3, 1),
    (11, 4, 2, 2),
    (12, 4, 1, 3),
    (13, 5, 3, 1),
    (14, 5, 2, 2),
    (15, 5, 1, 3)
ON CONFLICT (id_ronda) DO UPDATE
SET id_torneo        = EXCLUDED.id_torneo,
    id_zona_ambiental = EXCLUDED.id_zona_ambiental,
    numero_ronda     = EXCLUDED.numero_ronda;

-- ============================================
-- 5. Participaciones
--
-- t1: usuarios 2..13 (12)  -> IDs 1..12
-- t2: usuarios 2..13 (12)  -> IDs 13..24
-- t3: usuarios 2..7  (6)   -> IDs 25..30
-- t4: usuarios 8..10 (3)   -> IDs 31..33
-- t5: usuarios 11..12 (2)  -> IDs 34..35
-- ============================================

INSERT INTO participacion (
    id_participacion,
    id_usuario,
    id_torneo,
    puntaje_final,
    posicion_final
)
OVERRIDING SYSTEM VALUE
VALUES
    (1,  2,  1, NULL, NULL),
    (2,  3,  1, NULL, NULL),
    (3,  4,  1, NULL, NULL),
    (4,  5,  1, NULL, NULL),
    (5,  6,  1, NULL, NULL),
    (6,  7,  1, NULL, NULL),
    (7,  8,  1, NULL, NULL),
    (8,  9,  1, NULL, NULL),
    (9,  10, 1, NULL, NULL),
    (10, 11, 1, NULL, NULL),
    (11, 12, 1, NULL, NULL),
    (12, 13, 1, NULL, NULL),
    (13, 2,  2, NULL, NULL),
    (14, 3,  2, NULL, NULL),
    (15, 4,  2, NULL, NULL),
    (16, 5,  2, NULL, NULL),
    (17, 6,  2, NULL, NULL),
    (18, 7,  2, NULL, NULL),
    (19, 8,  2, NULL, NULL),
    (20, 9,  2, NULL, NULL),
    (21, 10, 2, NULL, NULL),
    (22, 11, 2, NULL, NULL),
    (23, 12, 2, NULL, NULL),
    (24, 13, 2, NULL, NULL),
    (25, 2,  3, NULL, NULL),
    (26, 3,  3, NULL, NULL),
    (27, 4,  3, NULL, NULL),
    (28, 5,  3, NULL, NULL),
    (29, 6,  3, NULL, NULL),
    (30, 7,  3, NULL, NULL),
    (31, 8,  4, NULL, NULL),
    (32, 9,  4, NULL, NULL),
    (33, 10, 4, NULL, NULL),
    (34, 11, 5, NULL, NULL),
    (35, 12, 5, NULL, NULL)
ON CONFLICT (id_participacion) DO UPDATE
SET id_usuario = EXCLUDED.id_usuario,
    id_torneo  = EXCLUDED.id_torneo;

-- Los torneos no finalizados nunca deben conservar una posicion historica.
UPDATE participacion
SET posicion_final = NULL
WHERE id_torneo IN (3, 4, 5)
  AND id_participacion BETWEEN 25 AND 35;

-- ============================================
-- 6. Puntajes y flechas
--
-- Se usa el procedimiento oficial para ejercitar todas las reglas de negocio.
-- Cada arquero dispara seis flechas en las tres rondas de t1, t2 y t3.
-- La formula es determinista y combina habilidad, variacion de tiro y clima:
--
--   12 - floor((usuario_id - 2) / 2)
--      - ((usuario_id + ronda_id + numero_flecha) % 4)
--      - penalizacion_climatica
--
-- La penalizacion es 0 para calma, 1 para lluvia y 2 para viento.
-- ============================================

DO $seed_scores$
DECLARE
    v_registro RECORD;
    v_flechas DECIMAL[];
    v_penalizacion INTEGER;
    v_longitud DOUBLE PRECISION;
    v_latitud DOUBLE PRECISION;
    v_offset_diana DOUBLE PRECISION;
BEGIN
    FOR v_registro IN
        SELECT
            p.id_participacion,
            p.id_usuario,
            p.id_torneo,
            r.id_ronda,
            t.id_categoria
        FROM participacion p
        JOIN ronda r
          ON r.id_torneo = p.id_torneo
        JOIN torneo t
          ON t.id_torneo = p.id_torneo
        WHERE p.id_participacion BETWEEN 1 AND 30
          AND p.id_torneo IN (1, 2, 3)
        ORDER BY r.id_ronda, p.id_usuario
    LOOP
        v_penalizacion := CASE MOD((v_registro.id_ronda - 1)::INTEGER, 3)
            WHEN 0 THEN 0 -- calma total
            WHEN 1 THEN 1 -- lluvia ligera
            ELSE 2        -- viento fuerte
        END;

        SELECT ARRAY_AGG(
                   GREATEST(
                       0,
                       LEAST(
                           10,
                           12
                           - ((v_registro.id_usuario - 2) / 2)::INTEGER
                           - MOD(
                               (v_registro.id_usuario + v_registro.id_ronda + numero_flecha)::INTEGER,
                               4
                           )
                           - v_penalizacion
                       )
                   )::DECIMAL
                   ORDER BY numero_flecha
               )
        INTO v_flechas
        FROM generate_series(1, 6) AS numero_flecha;

        -- Aproximadamente 9,3 m entre arqueros contiguos en la misma ronda.
        v_longitud := -70.64955
                      + ((v_registro.id_usuario - 2)::DOUBLE PRECISION * 0.00010);
        v_latitud := -33.44955
                     + (MOD((v_registro.id_ronda - 1)::INTEGER, 3) * 0.00035)
                     + ((v_registro.id_torneo - 1)::DOUBLE PRECISION * 0.00003);

        -- Distancias aproximadas: 14,5 m, 24,5 m, 44,5 m y 64,5 m.
        v_offset_diana := CASE v_registro.id_categoria
            WHEN 1 THEN 0.00013
            WHEN 2 THEN 0.00022
            WHEN 3 THEN 0.00040
            ELSE 0.00058
        END;

        CALL registrar_puntaje_ronda(
            v_registro.id_ronda,
            v_registro.id_participacion,
            v_flechas,
            1,
            ST_SetSRID(ST_MakePoint(v_longitud, v_latitud), 4326),
            ST_SetSRID(ST_MakePoint(v_longitud, v_latitud + v_offset_diana), 4326)
        );
    END LOOP;
END;
$seed_scores$;

-- Cerrar los historicos solo despues de registrar sus resultados.
UPDATE torneo
SET estado_torneo = CASE id_torneo
    WHEN 1 THEN 'COMPLETED'
    WHEN 2 THEN 'COMPLETED'
    WHEN 3 THEN 'IN_COURSE'
    ELSE 'NOT_STARTED'
END
WHERE id_torneo BETWEEN 1 AND 5;

CALL actualizar_posiciones(1);
CALL actualizar_posiciones(2);

-- Los cupos reflejan siempre las participaciones realmente almacenadas.
UPDATE torneo t
SET nro_plaza_actual = (
    SELECT COUNT(*)::INTEGER
    FROM participacion p
    WHERE p.id_torneo = t.id_torneo
)
WHERE t.id_torneo BETWEEN 1 AND 5;

-- ============================================
-- 7. Sincronizacion de secuencias
--
-- OVERRIDING SYSTEM VALUE no avanza las identidades. Se ajustan al maximo
-- actual para que las altas posteriores de la aplicacion no colisionen.
-- ============================================

SELECT setval(
    pg_get_serial_sequence('usuario', 'id_usuario'),
    GREATEST((SELECT COALESCE(MAX(id_usuario), 1) FROM usuario), 1),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('categoria', 'id_categoria'),
    GREATEST((SELECT COALESCE(MAX(id_categoria), 1) FROM categoria), 1),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('categoria_diana', 'id_categoria_diana'),
    GREATEST((SELECT COALESCE(MAX(id_categoria_diana), 1) FROM categoria_diana), 1),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('torneo', 'id_torneo'),
    GREATEST((SELECT COALESCE(MAX(id_torneo), 1) FROM torneo), 1),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('ronda', 'id_ronda'),
    GREATEST((SELECT COALESCE(MAX(id_ronda), 1) FROM ronda), 1),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('participacion', 'id_participacion'),
    GREATEST((SELECT COALESCE(MAX(id_participacion), 1) FROM participacion), 1),
    TRUE
);

-- ============================================
-- 8. Verificaciones atomicas
--
-- Cualquier inconsistencia aborta la transaccion completa.
-- ============================================

DO $seed_assertions$
DECLARE
    v_cantidad BIGINT;
BEGIN
    SELECT COUNT(*)
    INTO v_cantidad
    FROM usuario
    WHERE id_usuario BETWEEN 1 AND 13;

    IF v_cantidad <> 13 THEN
        RAISE EXCEPTION 'Seed invalido: se esperaban 13 usuarios y hay %.', v_cantidad;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM usuario
        WHERE id_usuario = 1
          AND rut = '1111111-1'
          AND contrasena = 'admin123'
          AND rol = 'ADMIN'
    ) OR NOT EXISTS (
        SELECT 1
        FROM usuario
        WHERE id_usuario = 2
          AND rut = '10000001-K'
          AND contrasena = 'arco123'
          AND rol = 'ARQUERO'
    ) THEN
        RAISE EXCEPTION 'Seed invalido: no coinciden las credenciales publicadas en README.md.';
    END IF;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM torneo
    WHERE (id_torneo IN (1, 2) AND estado_torneo = 'COMPLETED')
       OR (id_torneo = 3 AND estado_torneo = 'IN_COURSE')
       OR (id_torneo IN (4, 5) AND estado_torneo = 'NOT_STARTED');

    IF v_cantidad <> 5 THEN
        RAISE EXCEPTION 'Seed invalido: estados de torneo inesperados (% de 5 correctos).', v_cantidad;
    END IF;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM ronda
    WHERE id_ronda BETWEEN 1 AND 15
      AND numero_ronda BETWEEN 1 AND 3;

    IF v_cantidad <> 15 THEN
        RAISE EXCEPTION 'Seed invalido: se esperaban 15 rondas y hay %.', v_cantidad;
    END IF;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM participacion
    WHERE id_participacion BETWEEN 1 AND 35;

    IF v_cantidad <> 35 THEN
        RAISE EXCEPTION 'Seed invalido: se esperaban 35 participaciones y hay %.', v_cantidad;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM participacion
        WHERE id_torneo BETWEEN 1 AND 5
        GROUP BY id_torneo, id_usuario
        HAVING COUNT(*) > 1
    ) OR EXISTS (
        SELECT 1
        FROM ronda
        WHERE id_torneo BETWEEN 1 AND 5
        GROUP BY id_torneo, numero_ronda
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Seed invalido: hay participaciones o rondas duplicadas.';
    END IF;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM puntaje_ronda pr
    JOIN participacion p
      ON p.id_participacion = pr.id_participacion
    WHERE p.id_participacion BETWEEN 1 AND 30
      AND p.id_torneo IN (1, 2, 3);

    IF v_cantidad <> 90 THEN
        RAISE EXCEPTION 'Seed invalido: se esperaban 90 puntajes de ronda y hay %.', v_cantidad;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM puntaje_ronda pr
        JOIN ronda r
          ON r.id_ronda = pr.id_ronda
        JOIN participacion p
          ON p.id_participacion = pr.id_participacion
        WHERE p.id_participacion BETWEEN 1 AND 30
          AND r.id_torneo <> p.id_torneo
    ) THEN
        RAISE EXCEPTION 'Seed invalido: una ronda y su participacion pertenecen a torneos distintos.';
    END IF;

    SELECT COUNT(*)
    INTO v_cantidad
    FROM flecha f
    JOIN puntaje_ronda pr
      ON pr.id_puntaje_ronda = f.id_puntaje_ronda
    JOIN participacion p
      ON p.id_participacion = pr.id_participacion
    WHERE p.id_participacion BETWEEN 1 AND 30
      AND p.id_torneo IN (1, 2, 3);

    IF v_cantidad <> 540 THEN
        RAISE EXCEPTION 'Seed invalido: se esperaban 540 flechas y hay %.', v_cantidad;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM puntaje_ronda pr
        JOIN participacion p
          ON p.id_participacion = pr.id_participacion
        LEFT JOIN flecha f
          ON f.id_puntaje_ronda = pr.id_puntaje_ronda
        WHERE p.id_participacion BETWEEN 1 AND 30
        GROUP BY pr.id_puntaje_ronda, pr.puntaje_ronda
        HAVING COUNT(f.id_flecha) <> 6
            OR pr.puntaje_ronda <> COALESCE(SUM(f.puntaje), 0)
    ) THEN
        RAISE EXCEPTION 'Seed invalido: un puntaje no posee seis flechas o su suma no coincide.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM participacion p
        LEFT JOIN puntaje_ronda pr
          ON pr.id_participacion = p.id_participacion
        WHERE p.id_participacion BETWEEN 1 AND 30
        GROUP BY p.id_participacion, p.puntaje_final
        HAVING p.puntaje_final <> COALESCE(SUM(pr.puntaje_ronda), 0)
    ) THEN
        RAISE EXCEPTION 'Seed invalido: un puntaje final no coincide con la suma de sus rondas.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM participacion p
        WHERE p.id_torneo IN (1, 2)
          AND p.id_participacion BETWEEN 1 AND 24
          AND p.posicion_final IS NULL
    ) OR EXISTS (
        SELECT 1
        FROM participacion p
        WHERE p.id_torneo IN (3, 4, 5)
          AND p.id_participacion BETWEEN 25 AND 35
          AND p.posicion_final IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Seed invalido: posiciones finales incompatibles con el estado del torneo.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM torneo t
        WHERE t.id_torneo BETWEEN 1 AND 5
          AND (
              t.nro_plaza_actual <> (
                  SELECT COUNT(*)
                  FROM participacion p
                  WHERE p.id_torneo = t.id_torneo
              )
              OR t.nro_plaza_actual > t.nro_plaza_max
          )
    ) THEN
        RAISE EXCEPTION 'Seed invalido: cupos actuales inconsistentes.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM puntaje_ronda pr
        JOIN ronda r
          ON r.id_ronda = pr.id_ronda
        JOIN torneo t
          ON t.id_torneo = r.id_torneo
        JOIN categoria c
          ON c.id_categoria = t.id_categoria
        WHERE pr.id_participacion BETWEEN 1 AND 30
          AND (
              pr.posicion_arquero IS NULL
              OR pr.posicion_diana IS NULL
              OR NOT ST_IsValid(pr.posicion_arquero)
              OR NOT ST_IsValid(pr.posicion_diana)
              OR NOT ST_Contains(t.espacio_torneo, pr.posicion_arquero)
              OR ST_DistanceSphere(pr.posicion_arquero, pr.posicion_diana) > c.distancia_tiro
          )
    ) THEN
        RAISE EXCEPTION 'Seed invalido: geometria fuera del torneo o distancia de tiro excedida.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM puntaje_ronda pr1
        JOIN puntaje_ronda pr2
          ON pr2.id_ronda = pr1.id_ronda
         AND pr2.id_puntaje_ronda > pr1.id_puntaje_ronda
        JOIN participacion p1
          ON p1.id_participacion = pr1.id_participacion
        JOIN participacion p2
          ON p2.id_participacion = pr2.id_participacion
        WHERE p1.id_participacion BETWEEN 1 AND 30
          AND p2.id_participacion BETWEEN 1 AND 30
          AND ST_DistanceSphere(pr1.posicion_arquero, pr2.posicion_arquero) < 5.0
    ) THEN
        RAISE EXCEPTION 'Seed invalido: hay arqueros separados por menos de 5 metros.';
    END IF;
END;
$seed_assertions$;

-- Los rankings historicos ya estan disponibles al iniciar la aplicacion.
REFRESH MATERIALIZED VIEW leaderboard_top_50;

DO $leaderboard_assertion$
BEGIN
    IF (SELECT COUNT(*) FROM leaderboard_top_50) < 12 THEN
        RAISE EXCEPTION 'Seed invalido: el leaderboard deberia contener los 12 arqueros historicos.';
    END IF;
END;
$leaderboard_assertion$;

COMMIT;
