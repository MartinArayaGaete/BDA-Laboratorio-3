# Sistema de Gestión de Torneos de Tiro con Arco

Plataforma web del **Grupo 1** para administrar torneos, arqueros, rondas y puntuaciones. Integra React, Spring Boot, PostgreSQL/PostGIS, MongoDB Replica Set y Docker Compose.

## Integrantes

- Martín Alvayay — Frontend/Base de datos (NoSQL)
- Martin Araya — Base de datos (NoSQL)
- José Ceardi — Frontend/Backend
- Benjamín Letelier — Frontend
- Benjamín Paredes — SQL
- Nicolas Rojas — Backend

## Arquitectura

| Capa | Tecnología | Responsabilidad |
| --- | --- | --- |
| Frontend | React 19, Vite, React Router, Axios, Bootstrap y MapLibre | Vistas para administrador y arquero, mapas, formularios y estadísticas |
| Backend | Java 17, Spring Boot, JDBC, Spring Data MongoDB y JWT | API REST, reglas de negocio y conexión entre ambos motores |
| PostgreSQL/PostGIS | PostgreSQL 16 + PostGIS 3.4 | Usuarios, categorías, auditoría, datos geoespaciales y consultas SQL avanzadas |
| MongoDB | MongoDB 7, Replica Set `rs0` | Torneos operacionales, rondas, participaciones, puntuaciones y ranking en vivo |
| Orquestación | Docker Compose | Inicialización reproducible de todos los servicios |

PostgreSQL conserva los catálogos relacionales y las reglas SQL; MongoDB concentra el flujo operativo que consume el frontend para torneos, rondas, inscripciones y puntajes. Los servicios Mongo validan las referencias que dependen de PostgreSQL, como usuario, categorías y zonas ambientales.

## Funcionalidades

### Administración y competencia

- Crear, editar, iniciar y finalizar torneos.
- Gestionar categorías de distancia y de diana.
- Inscribir o desinscribir arqueros.
- Crear rondas, asignar una zona ambiental y avanzar a la siguiente ronda.
- Registrar seis flechas, la posición del arquero y la posición de la diana sobre el mapa.
- Calcular puntaje acumulado, ranking en vivo y podio al finalizar un torneo.
- Consultar auditoría, categorías ambientales y correlación climática.

## Requisitos

Para el modo estándar solo se requiere Docker con el complemento Docker Compose.

- Linux: Docker Engine y Docker Compose.
- macOS o Windows: Docker Desktop; en Windows se recomienda la integración con WSL2.

## Inicio rápido

Desde la raíz del repositorio:

```bash
docker compose -f infrastructure-archery/docker-compose.yml up -d --build
```

Comprueba el estado del arranque:

```bash
docker compose -f infrastructure-archery/docker-compose.yml ps
docker compose -f infrastructure-archery/docker-compose.yml logs --no-color database-archery mongo-replica-set-init backend-archery
```

El inicializador `mongo-replica-set-init` debe finalizar con código `0`; luego el backend inicia cuando PostgreSQL y MongoDB ya están disponibles.

| Servicio | URL o conexión por defecto | Credenciales por defecto |
| --- | --- | --- |
| Frontend | http://localhost:3000 | Ver credenciales de prueba abajo |
| Backend | http://localhost:8080 | — |
| PostgreSQL | `localhost:5432`, base `archeryDb` | `archeryUser` / `12345` |
| pgAdmin | http://localhost:5050 | `example@gmail.com` / `12345` |
| MongoDB primario | `mongodb://localhost:27017/archerydb` | Sin autenticación local |
| MongoDB secundario | `mongodb://localhost:27018/archerydb` | Sin autenticación local |
| Mongo Express | http://localhost:8081 | `admin` / `12345` |

## Población inicial

Con los volúmenes vacíos, la composición ejecuta este orden:

1. PostgreSQL ejecuta los scripts `database/sql/01-dbCreate.sql` a `database/sql/11-testData.sql`.
2. Se crea el Replica Set de MongoDB `rs0`.
3. El inicializador Mongo espera a PostgreSQL y aplica, en este orden:
   - `database/mongo/01-SchemaValidation.js`
   - `database/mongo/02-Indexes.js`
   - `database/mongo/04-SeedData.js`
   - `database/mongo/03-ArcherRendimientoPipeline.js`
4. El backend comienza después de que el inicializador Mongo termina correctamente.

El seed deja datos consistentes entre los motores:

| Recurso | Cantidad |
| --- | ---: |
| Usuarios | 13 |
| Categorías de distancia | 4 |
| Categorías de diana | 3 |
| Torneos | 5 |
| Rondas | 15 |
| Participaciones | 35 |
| Puntuaciones de ronda | 90 |
| Flechas | 540 |

Los IDs de torneo y ronda de MongoDB son cadenas que reutilizan el ID de PostgreSQL: por ejemplo, el torneo `3` se representa como `"3"` en MongoDB. Los estados se relacionan de esta forma:

| PostgreSQL | MongoDB |
| --- | --- |
| `NOT_STARTED` | `PENDIENTE` |
| `IN_COURSE` | `IN_COURSE` |
| `COMPLETED` | `FINISHED` |

Los torneos de prueba son:

| ID Mongo | Torneo | Estado Mongo |
| --- | --- | --- |
| `"1"` | Copa Santiago Histórica | `FINISHED` |
| `"2"` | Campeonato Metropolitano | `FINISHED` |
| `"3"` | Liga de Invierno | `IN_COURSE` |
| `"4"` | Torneo Primavera | `PENDIENTE` |
| `"5"` | Copa de los Andes | `PENDIENTE` |

Para verificar la carga:

```bash
docker compose -f infrastructure-archery/docker-compose.yml exec -T database-archery sh -lc 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT COUNT(*) AS usuarios FROM usuario;"'
docker compose -f infrastructure-archery/docker-compose.yml exec -T mongo-archery mongosh --quiet --eval 'db.getSiblingDB("archerydb").puntuaciones.countDocuments()'
```

PostgreSQL solo ejecuta los scripts de `/docker-entrypoint-initdb.d` al crear un volumen nuevo. Para conservar los datos al detener el entorno:

```bash
docker compose -f infrastructure-archery/docker-compose.yml down
```

Para reconstruir las dos bases desde cero:

```bash
docker compose -f infrastructure-archery/docker-compose.yml down -v
docker compose -f infrastructure-archery/docker-compose.yml up -d --build
```

> **Advertencia:** `down -v` elimina los volúmenes de PostgreSQL y de ambos nodos MongoDB.

## Accesos de prueba

| Rol | RUT | Contraseña |
| --- | --- | --- |
| Administrador | `1111111-1` | `admin123` |
| Arquero | `10000001-K` | `arco123` |

La pantalla de login redirige a `/admin` o `/archer` según el rol del usuario.

## API usada por el frontend

La URL base por defecto es `http://localhost:8080/api`. La aplicación usa PostgreSQL/PostGIS para usuarios, categorías, auditoría y geodatos; MongoDB se usa para el flujo operativo de competencia.

### Recursos MongoDB

| Recurso | Rutas principales | Uso |
| --- | --- | --- |
| Torneos | `GET/POST /mongo/torneos`, `GET/PUT/DELETE /mongo/torneos/{id}` | Consultar y administrar torneos |
| Ciclo de torneo | `PUT /mongo/torneos/{id}/iniciar`, `/siguiente-ronda`, `/finalizar` | Iniciar, avanzar y finalizar |
| Rondas | `GET /mongo/rondas/torneo/{torneoId}`, `POST /mongo/rondas` | Consultar o crear rondas |
| Estado de ronda | `PUT /mongo/rondas/{id}/iniciar`, `/finalizar` | Cambiar estado de una ronda |
| Zona ambiental | `PUT /mongo/rondas/{id}/zona-ambiental` | Asociar una zona PostGIS a la ronda |
| Participaciones | `GET/POST /mongo/participaciones`, `DELETE /mongo/participaciones/{torneoId}/{usuarioId}` | Listar, inscribir o desinscribir |
| Puntuaciones | `POST /mongo/puntuaciones/registrar` | Registrar o actualizar flechas y ubicaciones |
| Posiciones | `GET /mongo/torneos/{id}/rondas/{numero}/posiciones` | Ubicaciones de los arqueros de una ronda |
| Puntaje individual | `GET /mongo/torneos/{id}/rondas/{numero}/arqueros/{usuarioId}/posicion` | Precarga de flechas y ubicaciones |
| Historial de arquero | `GET /mongo/arqueros/{usuarioId}/historial?page=0&size=5` | Historial paginado |
| Estadísticas de arquero | `GET /mongo/arqueros/{usuarioId}/estadisticas` | Métricas personales |
| Ranking en vivo | `GET /mongo/ranking/torneo/{torneoId}` | Ranking acumulado del torneo |

#### Cuerpos relevantes

Crear o actualizar un torneo:

```json
{
  "nombre": "Torneo de ejemplo",
  "fechaInicio": "2026-08-10",
  "fechaTermino": "2026-08-11",
  "plazasMax": 20,
  "categoriaDistanciaId": 1,
  "categoriaDianaId": 1,
  "zonaCompetenciaGeoJSON": "{\"type\":\"Polygon\",\"coordinates\":[...]}",
  "lineaTiroGeoJSON": "{\"type\":\"LineString\",\"coordinates\":[...]}"
}
```

Inscribir un arquero:

```json
{
  "torneoId": "3",
  "usuarioId": 2
}
```

Crear una ronda y asignar su zona ambiental:

```json
{ "torneoId": "3", "numeroRonda": 4 }
```

```json
{ "idZonaAmbiental": 1 }
```

Aunque el documento de ronda persiste el campo `postgisZonaId`, el cuerpo del endpoint usa `idZonaAmbiental`. Para registrar una puntuación debe ser una zona válida, no `null`.

Registrar las seis flechas:

```json
{
  "torneoId": "3",
  "rondaId": "9",
  "usuarioId": 2,
  "flechas": [8, 9, 7, 10, 8, 9],
  "posicionArquero": "{\"type\":\"Point\",\"coordinates\":[-70.64955,-33.44885]}",
  "posicionDiana": "{\"type\":\"Point\",\"coordinates\":[-70.64955,-33.44845]}"
}
```

El frontend valida seis valores enteros de `0` a `10`; el backend además valida la inscripción, el torneo en curso, la zona ambiental, el mínimo de la categoría de diana y la geometría configurada.

### Recursos PostgreSQL/PostGIS

| Recurso | Rutas principales |
| --- | --- |
| Autenticación | `POST /auth/login`, `POST /auth/logout`, `POST /auth/refresh-token` |
| Usuarios | CRUD en `/usuarios` y listado por rol en `/usuarios/rol/{rol}` |
| Categorías de distancia | CRUD en `/categorias` |
| Categorías de diana | CRUD en `/categorias-diana` |
| Mapas y zonas | `/mapas/torneos/{id}`, `/mapas/zonas-ambientales`, `/mapas/categorias-por-coordenada` |
| Ambiental | CRUD en `/categorias-ambientales` y `/sectores-ambientales` |
| Auditoría | `GET /logs?page={page}&size={size}` |
| Correlación ambiental | `GET /estadisticas/correlacion-ambiental` |
| Flujo relacional histórico | `/torneos`, `/rondas`, `/participaciones`, `/arqueros` |

Los endpoints relacionales de torneo tienen contratos distintos de los MongoDB. Por ejemplo, el registro SQL de puntaje usa `/torneos/registrar-puntaje`, mientras que el detalle de torneo del frontend usa `/mongo/puntuaciones/registrar`.

## Flujo correcto de una ronda MongoDB

1. Crea el torneo en estado `PENDIENTE`, al menos una ronda y las participaciones necesarias.
2. Inicia el torneo con `PUT /api/mongo/torneos/{id}/iniciar`. El servicio deja la primera ronda en `IN_COURSE`.
3. Asigna a la ronda una zona ambiental con `PUT /api/mongo/rondas/{rondaId}/zona-ambiental`.
4. Registra las seis flechas y ambas posiciones mediante `POST /api/mongo/puntuaciones/registrar`.
5. Para continuar, usa `PUT /api/mongo/torneos/{id}/siguiente-ronda`: finaliza la ronda activa e inicia la siguiente pendiente.
6. Finaliza el torneo con `PUT /api/mongo/torneos/{id}/finalizar`; se calculan puntajes finales y podio.

Este orden evita errores como `El torneo no está en curso` y asegura que la ronda que recibe las flechas tenga una zona ambiental asociada.

## Pipeline de rendimiento MongoDB

La pantalla administrativa consulta:

```text
GET /api/mongo/pipeline/rendimiento
```

El endpoint ejecuta una agregación `$facet` sobre la colección `puntuaciones` y devuelve un arreglo con un único documento:

```json
[
  {
    "rendimientoPorTorneo": [],
    "rendimientoPorCategoria": [],
    "distribucionPorRendimiento": []
  }
]
```

Cada elemento de `rendimientoPorTorneo` incluye `torneoId`, `usuarioId`, nombre del torneo y arquero, `promedioPuntaje`, `mejorRonda` y `rondasRegistradas`. La sección por categoría usa `categoria` y `rondasConsideradas`; la distribución entrega cada rango, su cantidad y la lista de arqueros.

El resultado se calcula al solicitar el endpoint; no se materializa una colección adicional de estadísticas. El script `database/mongo/03-ArcherRendimientoPipeline.js` se ejecuta durante la inicialización para verificar e imprimir el resultado del mismo pipeline.

> La ruta `/api/mongo/puntuaciones/estadisticas` mantiene una agregación anterior de formato plano. La pantalla de estadísticas usa `/api/mongo/pipeline/rendimiento`.

Puedes revisar el resultado directamente:

```bash
curl http://localhost:8080/api/mongo/pipeline/rendimiento
```

## Reglas de datos relevantes

- Una participación MongoDB debe referenciar a un usuario existente en PostgreSQL.
- Un torneo MongoDB obtiene sus categorías de distancia y diana desde PostgreSQL.
- Una ronda guarda el ID de una zona ambiental PostGIS en `postgisZonaId`.
- La combinación de torneo, ronda y usuario es única para una puntuación MongoDB; registrar otra vez la misma combinación actualiza el documento.
- Las flechas deben estar entre `0` y `10`; la categoría de diana puede imponer un mínimo mayor.
- Las coordenadas se validan contra la zona de competencia y la distancia de tiro de la categoría.
- PostgreSQL mantiene procedimientos, triggers, auditoría y la vista materializada `leaderboard_top_50` para el flujo relacional.

## Variables de entorno

Los valores por defecto del archivo Compose permiten ejecutar el proyecto sin configuración adicional.

| Variable | Valor por defecto | Nota |
| --- | --- | --- |
| `FRONT_PORT` | `3000` | Puerto publicado de Vite |
| `BACK_PORT` | `8080` | Debe coincidir con la URL configurada en `frontend/mi-app/src/api/api.js` |
| `POSTGRES_DB` | `archeryDb` | Base de datos PostgreSQL |
| `POSTGRES_USER` | `archeryUser` | Usuario PostgreSQL |
| `POSTGRES_PASSWORD` | `12345` | Contraseña PostgreSQL de desarrollo |
| `PGADMIN_PORT` | `5050` | Puerto publicado de pgAdmin |
| `APP_TIMEZONE` | `America/Santiago` | Zona horaria de los servicios |

`infrastructure-archery/env.txt` es una plantilla de referencia; Docker Compose no la carga automáticamente. Si se usa con `--env-file`, ajusta `BACK_PORT` a `8080` o modifica el `baseURL` del cliente Axios. Mantén `POSTGRES_PORT=5432`: en la composición actual esa variable se reutiliza como puerto interno del backend y como puerto publicado de PostgreSQL.

## Verificación de código

```bash
docker compose -f infrastructure-archery/docker-compose.yml config --quiet
cd backend/demo && ./mvnw -DskipTests compile
cd frontend/mi-app && npm run build
```

## Nota de seguridad

El login genera una cookie JWT y el frontend separa las rutas de administrador y arquero. Sin embargo, la configuración actual del backend permite todas las solicitudes (`anyRequest().permitAll()`) para desarrollo. No debe utilizarse tal cual en un entorno de producción; allí se debe habilitar el filtro JWT y las reglas RBAC del backend.
