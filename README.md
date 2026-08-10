# Sistema de Gestión de Torneos de Tiro con Arco

Manual de instalación y puesta en marcha del proyecto del **Grupo 1**. La aplicación usa React, Spring Boot, PostgreSQL/PostGIS, MongoDB Replica Set y Docker Compose.

## Integrantes

- Martín Alvayay: Frontend/Base de datos NoSQL
- Martin Araya: Base de datos NoSQL
- José Ceardi: Frontend/Backend
- Benjamín Letelier: Frontend
- Benjamín Paredes: SQL
- Nicolas Rojas: Backend

## 1. Requisitos

La instalación recomendada es con Docker. No es necesario instalar Java, Node, PostgreSQL ni MongoDB en la máquina anfitriona.

| Requisito | Versión recomendada | Uso |
| --- | --- | --- |
| Docker Engine o Docker Desktop | Actual | Ejecutar los contenedores |
| Docker Compose | V2 | Orquestar frontend, backend y bases de datos |
| Git | Actual | Clonar el repositorio |

En Windows se recomienda usar Docker Desktop con integración WSL2.

## 2. Clonar el proyecto

```bash
git clone <url-del-repositorio>
cd BDA-Laboratorio-3
```

Todos los comandos siguientes se ejecutan desde la raíz del repositorio.

## 3. Levantar la aplicación

```bash
docker compose -f infrastructure-archery/docker-compose.yml up -d --build
```

Este comando construye y levanta:

| Servicio | Contenedor | Puerto |
| --- | --- | --- |
| Frontend React/Vite | `frontend-archery` | `3000` |
| Backend Spring Boot | `backend-archery` | `8080` |
| PostgreSQL/PostGIS | `database-archery` | `5432` |
| pgAdmin | `pgadmin` | `5050` |
| MongoDB primario | `mongo-archery` | `27017` |
| MongoDB secundario | `mongo-archery-secondary` | `27018` |
| Inicializador Mongo Replica Set | `mongo-replica-set-init` | Sin puerto |
| Mongo Express | `mongo-express` | `8081` |

El backend se inicia después de que PostgreSQL termine su seed y después de que MongoDB quede configurado como Replica Set.

## 4. Verificar que todo inició correctamente

Revisa el estado de los servicios:

```bash
docker compose -f infrastructure-archery/docker-compose.yml ps
```

El contenedor `mongo-replica-set-init` debe aparecer como finalizado correctamente, normalmente con estado `Exited (0)`. Los servicios `frontend-archery`, `backend-archery`, `database-archery`, `mongo-archery`, `mongo-archery-secondary`, `pgadmin` y `mongo-express` deben quedar activos.

Si algo no inicia, revisa logs:

```bash
docker compose -f infrastructure-archery/docker-compose.yml logs --no-color database-archery mongo-replica-set-init backend-archery frontend-archery
```

Pruebas rápidas de datos:

```bash
docker compose -f infrastructure-archery/docker-compose.yml exec -T database-archery sh -lc 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT COUNT(*) AS usuarios FROM usuario;"'
```

```bash
docker compose -f infrastructure-archery/docker-compose.yml exec -T mongo-archery mongosh --quiet --eval 'db.getSiblingDB("archerydb").puntuaciones.countDocuments()'
```

Valores esperados con el seed inicial:

| Dato | Cantidad |
| --- | ---: |
| Usuarios | 13 |
| Torneos | 5 |
| Rondas | 15 |
| Participaciones | 35 |
| Puntuaciones | 90 |
| Flechas | 540 |

## 5. Entrar a la aplicación

Abre el frontend:

```text
http://localhost:3000
```

Credenciales de prueba:

| Rol | RUT | Contraseña |
| --- | --- | --- |
| Administrador | `1111111-1` | `admin123` |
| Arquero | `10000001-K` | `arco123` |

Servicios auxiliares:

| Servicio | URL | Credenciales |
| --- | --- | --- |
| Backend API | `http://localhost:8080/api` | No aplica |
| pgAdmin | `http://localhost:5050` | `example@gmail.com` / `12345` |
| Mongo Express | `http://localhost:8081` | `admin` / `12345` |

## 6. Datos cargados al instalar

La instalación con volúmenes vacíos ejecuta automáticamente los scripts de población.

PostgreSQL ejecuta los archivos de `database/sql` en orden alfabético, desde `01-dbCreate.sql` hasta `11-testData.sql`. MongoDB ejecuta:

```text
database/mongo/01-SchemaValidation.js
database/mongo/02-Indexes.js
database/mongo/04-SeedData.js
database/mongo/03-ArcherRendimientoPipeline.js
```

Torneos iniciales:

| ID Mongo | Torneo | Estado |
| --- | --- | --- |
| `"1"` | Copa Santiago Histórica | `FINISHED` |
| `"2"` | Campeonato Metropolitano | `FINISHED` |
| `"3"` | Liga de Invierno | `IN_COURSE` |
| `"4"` | Torneo Primavera | `PENDIENTE` |
| `"5"` | Copa de los Andes | `PENDIENTE` |

Relación de estados entre motores:

| PostgreSQL | MongoDB |
| --- | --- |
| `NOT_STARTED` | `PENDIENTE` |
| `IN_COURSE` | `IN_COURSE` |
| `COMPLETED` | `FINISHED` |

Los IDs usados por MongoDB para torneos y rondas son strings, por ejemplo `"3"`. Los IDs referenciales hacia usuarios, categorías y zonas ambientales corresponden a IDs creados en PostgreSQL.

## 7. Flujo básico para probar una ronda

Con los datos iniciales, el torneo `"3"` queda en curso y tiene rondas creadas. El flujo esperado desde el frontend es:

1. Entrar como administrador.
2. Abrir un torneo en curso.
3. Seleccionar una ronda en estado `IN_COURSE`.
4. Seleccionar un arquero inscrito.
5. Ubicar al arquero y la diana en el mapa.
6. Registrar las seis flechas.
7. Avanzar a la siguiente ronda o finalizar el torneo.

El backend valida que el usuario esté inscrito, que el torneo esté en curso, que la ronda pertenezca al torneo, que la zona ambiental exista, que las flechas respeten la categoría de diana y que las posiciones cumplan la distancia de tiro.

## 8. Endpoints útiles para verificar la instalación

La URL base del frontend apunta a:

```text
http://localhost:8080/api
```

Endpoints principales usados durante la prueba:

| Acción | Endpoint |
| --- | --- |
| Login | `POST /api/auth/login` |
| Listar torneos Mongo | `GET /api/mongo/torneos` |
| Iniciar torneo | `PUT /api/mongo/torneos/{id}/iniciar` |
| Avanzar ronda | `PUT /api/mongo/torneos/{id}/siguiente-ronda` |
| Finalizar torneo | `PUT /api/mongo/torneos/{id}/finalizar` |
| Rondas de torneo | `GET /api/mongo/rondas/torneo/{torneoId}` |
| Inscripciones | `GET /api/mongo/participaciones/torneo/{torneoId}` |
| Registrar puntaje | `POST /api/mongo/puntuaciones/registrar` |
| Precargar puntaje de arquero | `GET /api/mongo/torneos/{id}/rondas/{numero}/arqueros/{usuarioId}/posicion` |
| Ranking en vivo | `GET /api/mongo/ranking/torneo/{torneoId}` |
| Estadísticas pipeline | `GET /api/mongo/pipeline/rendimiento` |

Ejemplo para probar el pipeline de estadísticas:

```bash
curl http://localhost:8080/api/mongo/pipeline/rendimiento
```

Ese endpoint devuelve tres sectores: `rendimientoPorTorneo`, `rendimientoPorCategoria` y `distribucionPorRendimiento`.

## 9. Variables de entorno

La composición funciona sin crear archivos adicionales porque cada variable tiene valor por defecto.

| Variable | Valor por defecto | Descripción |
| --- | --- | --- |
| `FRONT_PORT` | `3000` | Puerto publicado del frontend |
| `BACK_PORT` | `8080` | Puerto publicado del backend |
| `POSTGRES_HOST` | `database-archery` | Host usado por el backend dentro de Docker |
| `POSTGRES_PORT` | `5432` | Puerto PostgreSQL interno y publicado |
| `POSTGRES_DB` | `archeryDb` | Base PostgreSQL |
| `POSTGRES_USER` | `archeryUser` | Usuario PostgreSQL |
| `POSTGRES_PASSWORD` | `12345` | Contraseña PostgreSQL |
| `PGADMIN_PORT` | `5050` | Puerto publicado de pgAdmin |
| `APP_TIMEZONE` | `America/Santiago` | Zona horaria de la aplicación |

`infrastructure-archery/env.txt` es solo una plantilla. Docker Compose no la carga automáticamente. Si se usa con `--env-file infrastructure-archery/env.txt`, hay que cuidar que `BACK_PORT` quede en `8080` o ajustar `frontend/mi-app/src/api/api.js`, porque el frontend tiene configurado `http://localhost:8080/api`.

## 10. Detener, reiniciar y reconstruir

Detener sin borrar datos:

```bash
docker compose -f infrastructure-archery/docker-compose.yml down
```

Volver a levantar conservando datos:

```bash
docker compose -f infrastructure-archery/docker-compose.yml up -d
```

Reconstruir imágenes:

```bash
docker compose -f infrastructure-archery/docker-compose.yml up -d --build
```

Recrear las bases de datos desde cero:

```bash
docker compose -f infrastructure-archery/docker-compose.yml down -v
docker compose -f infrastructure-archery/docker-compose.yml up -d --build
```

`down -v` elimina los volúmenes de PostgreSQL y MongoDB. Úsalo solo cuando quieras volver al seed inicial.

## 11. Instalación local para desarrollo

Docker sigue siendo necesario para PostgreSQL/PostGIS y MongoDB, pero se puede ejecutar frontend o backend fuera de contenedores.

Levantar solo bases de datos y herramientas:

```bash
docker compose -f infrastructure-archery/docker-compose.yml up -d database-archery mongo-archery mongo-archery-secondary mongo-replica-set-init pgadmin mongo-express
```

Backend local:

```bash
cd backend/demo
./mvnw spring-boot:run
```

El backend local necesita las mismas variables de conexión. Como `application.properties` usa por defecto `POSTGRES_HOST=database-archery`, para correr fuera de Docker se debe usar `POSTGRES_HOST=localhost`.

Frontend local:

```bash
cd frontend/mi-app
npm install
npm run dev
```

El cliente Axios está configurado en `frontend/mi-app/src/api/api.js` con:

```text
http://localhost:8080/api
```

## 12. Comandos de validación

Validar la configuración de Compose:

```bash
docker compose -f infrastructure-archery/docker-compose.yml config --quiet
```

Compilar backend:

```bash
cd backend/demo
./mvnw -DskipTests compile
```

Compilar frontend:

```bash
cd frontend/mi-app
npm run build
```

## 13. Problemas comunes

**El frontend abre, pero no carga datos:** verifica que el backend esté en `http://localhost:8080` y que `frontend/mi-app/src/api/api.js` apunte a ese puerto.

**El backend no inicia:** revisa los logs de `database-archery`, `mongo-replica-set-init` y `backend-archery`. El backend espera a que PostgreSQL tenga el usuario seed `1111111-1` y a que MongoDB termine el Replica Set.

**Los datos no se regeneran después de cambiar scripts SQL:** PostgreSQL solo ejecuta `/docker-entrypoint-initdb.d` cuando el volumen está vacío. Ejecuta `docker compose -f infrastructure-archery/docker-compose.yml down -v` y vuelve a levantar.

**MongoDB no permite Change Streams:** el proyecto usa MongoDB como Replica Set `rs0`; si se ejecuta MongoDB manualmente sin Replica Set, los listeners de change stream no funcionarán.

**Puerto ocupado:** define otro puerto antes del comando, por ejemplo:

```bash
FRONT_PORT=3001 docker compose -f infrastructure-archery/docker-compose.yml up -d frontend-archery
```

Si cambias `BACK_PORT`, también debes cambiar el `baseURL` del frontend.

## 14. Nota de seguridad

Las credenciales incluidas son solo para desarrollo y demostración. La configuración actual permite solicitudes de desarrollo y no debe usarse como configuración productiva sin endurecer autenticación, autorización, secretos y exposición de puertos.
