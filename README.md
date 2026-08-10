# Sistema de Gestión de Torneos de Tiro con Arco

## Integrantes

- Martín Alvayay - **SQL**
- Martin Araya - **Frontend**
- José Ceardi - **Frontend**
- Benjamín Letelier - **Backend**
- Benjamín Paredes - **SQL**
- Nicolas Rojas - **Backend**

---

Plataforma web desarrollada por el **Grupo 1** para administrar arqueros, categorías, torneos, rondas y puntuaciones, integrando una arquitectura completa con **frontend**, **backend**, **PostgreSQL/PostGIS**, **MongoDB Replica Set** y **Docker Compose**.

La aplicación fue diseñada para cubrir un flujo real de competencia deportiva, con roles diferenciados para **Administrador** y **Arquero**, autenticación basada en **JWT** y reglas de negocio reforzadas directamente en la base de datos mediante **procedimientos almacenados, triggers, vistas materializadas e índices**.

## Alineación con el enunciado del laboratorio

Este proyecto responde al laboratorio de manera directa en los siguientes puntos:

- **Arquitectura multi-capa**: frontend React, backend Spring Boot, PostgreSQL/PostGIS y MongoDB.
- **Seguridad JWT + RBAC**: las rutas y acciones se separan por rol, distinguiendo administrador y arquero.
- **SQL avanzado**: procedimientos almacenados, triggers, vista materializada e índices aplicados al dominio del torneo.
- **Sin ORM**: el backend trabaja con acceso directo a la base de datos usando JDBC.
- **Despliegue reproducible**: todo el sistema se levanta con Docker Compose.
- **Evaluación del manual**: el README incluye los pasos necesarios para ejecutar el sistema en un entorno local siguiendo exclusivamente la documentación.

## Qué hace esta aplicación

Este sistema permite gestionar el ciclo completo de un torneo de tiro con arco:

- CRUD completo de **arqueros** y **torneos**.
- Gestión de **categorías** como Recurvo y Compuesto.
- Autenticación con **JWT** y control de acceso por rol.
- Vista personalizada para cada arquero, donde solo puede consultar su propio historial y rendimiento.
- Panel de administración para crear y administrar torneos.
- Registro de puntuaciones por ronda y cálculo de posiciones.
- Consulta de rankings, podios y leaderboard histórico.
- Validaciones de negocio reforzadas en PostgreSQL para asegurar integridad de datos.

## Funcionalidades principales

### Frontend

- Pantalla de **login**.
- Vista **Mi Perfil** para arqueros.
- Panel de **administración de torneos**.
- Formulario para **crear torneos**.
- Vista de **detalle de torneo**.
- **Leaderboard** general.
- Navegación adaptada según el rol del usuario.

### Backend

- API REST para **arqueros**, **torneos**, **categorías**, **participaciones**, **rondas** y **usuarios**.
- Autenticación y autorización con **Spring Security + JWT**.
- Integración con PostgreSQL para operaciones de negocio y consultas avanzadas.

## API principal

Rutas principales expuestas por el backend:

- **Auth**: `/api/auth/login`, `/api/auth/logout`
- **Usuarios**: `/api/usuarios`, `/api/usuarios/rol/{rol}`
- **Categorías**: `/api/categorias`
- **Torneos**: `/api/torneos`, `/api/torneos/registrar-puntaje`, `/api/torneos/{idTorneo}/podio`
- **Rondas**: `/api/rondas`
- **Participaciones**: `/api/participaciones`
- **Arqueros**: `/api/arqueros/{idUsuario}/historial`, `/api/arqueros/{idUsuario}/estadisticas`, `/api/arqueros/rendimiento/ultimo-mes`

### Base de datos

- Procedimiento almacenado para registrar la **puntuación completa de una ronda**.
- Procedimiento almacenado para **calcular y actualizar el ranking** al finalizar un torneo.
- Trigger para impedir flechas fuera de rango: **0 a 10** o **M**.
- Trigger de auditoría para registrar cambios sobre la **puntuación final** de un arquero.
- Vista materializada de **Leaderboard Histórico** con el promedio de puntos por flecha de los top 50 arqueros.
- Índices para optimizar consultas por **categoría del torneo** y por **identificador del arquero**.

## Reglas de negocio que contempla el proyecto

1. API CRUD completa de Arqueros y Torneos.
2. API/JWT donde el arquero solo ve su propio historial y el administrador puede crear torneos.
3. Procedimiento almacenado para registrar la puntuación de una ronda completa validando que el torneo esté activo.
4. Procedimiento almacenado para calcular y actualizar la tabla de posiciones al finalizar un torneo.
5. Trigger para impedir puntuaciones de flecha superiores a 10, inferiores a 0 o distintas de `M`.
6. Trigger de auditoría cuando un administrador modifica la puntuación final de un arquero.
7. Vista materializada con el leaderboard histórico de los top 50 arqueros.
8. Índices para acelerar consultas por categoría y arquero.
9. Endpoint para listar arqueros con mejor rendimiento del último mes.
10. Endpoint para devolver el podio de un torneo usando el cálculo de la base de datos.

## Arquitectura general

| Capa | Tecnologías | Responsabilidad |
| --- | --- | --- |
| Frontend | React 19, Vite, React Router, Axios, Bootstrap 5 | Interfaz de usuario y navegación por rol |
| Backend | Java 17, Spring Boot, Spring Security, JDBC, JWT | API REST, autenticación y lógica de negocio |
| Base de datos | PostgreSQL/PostGIS 16 y MongoDB 7 Replica Set | Datos relacionales/geoespaciales y documentos operacionales |
| Orquestación | Docker Compose | Levantar todo el stack de forma reproducible |

## Estructura del proyecto

```text
.
├── backend/demo/              # API Spring Boot
├── database/                  # Scripts SQL de creación, vistas, procedimientos y carga inicial
├── frontend/mi-app/           # Aplicación React
└── infrastructure-archery/    # Docker Compose y configuración del entorno
```

## Requisitos

No necesitas instalar Java, Node ni PostgreSQL de forma local para ejecutar el sistema en modo estándar. Solo necesitas:

- **Docker**
- **Docker Compose**

### Recomendación por sistema operativo

- **Linux**: instala Docker Engine y el complemento de Docker Compose. El arranque se hace desde la terminal.
- **macOS**: instala **Docker Desktop** o el stack Docker equivalente. Puedes iniciar el proyecto desde la terminal o desde la interfaz de Docker Desktop.
- **Windows**: instala **Docker Desktop** con integración para **WSL2**. Ejecuta los comandos desde PowerShell, Windows Terminal o la terminal de VS Code.

## Cómo iniciar el proyecto

### Opción recomendada: terminal

Desde la raíz del repositorio ejecuta:

```bash
docker compose -f infrastructure-archery/docker-compose.yml up -d --build
```

Ese comando levanta:

- el **frontend** en `http://localhost:3000`
- el **backend** en `http://localhost:8080`
- **PostgreSQL** en `localhost:5432`
- **pgAdmin** en `http://localhost:5050`
- **MongoDB primario** en `localhost:27017`
- **MongoDB secundario** en `localhost:27018`
- **Mongo Express** en `http://localhost:8081`

### Opción con Docker Desktop

Si usas Docker Desktop, puedes abrir el proyecto y ejecutar el mismo `docker compose` desde la terminal integrada, o iniciar la composición desde la interfaz si tu versión lo permite. En ambos casos, el stack es el mismo; solo cambia la forma de dispararlo.

## Detener el entorno

```bash
docker compose -f infrastructure-archery/docker-compose.yml down
```

Si además quieres eliminar todos los datos persistidos y volver a ejecutar la población inicial, usa:

```bash
docker compose -f infrastructure-archery/docker-compose.yml down -v
```

> **Advertencia:** `down -v` elimina tanto la base PostgreSQL como los dos volúmenes de MongoDB.

## Población inicial de datos

En una instalación limpia, Docker ejecuta automáticamente:

1. Los scripts `database/sql/01-*.sql` a `database/sql/11-testData.sql` en PostgreSQL.
2. La creación del Replica Set de MongoDB.
3. Los validadores, índices y `database/mongo/04-SeedData.js`.
4. El pipeline de estadísticas sobre los documentos ya cargados.

La población usa identificadores deterministas compartidos entre ambos motores. Incluye 13 usuarios, 4 categorías de distancia, 3 categorías de diana, 5 torneos, 15 rondas, 35 participaciones, 90 puntuaciones de ronda y 540 flechas, además de zonas PostGIS, podios y ranking en vivo.

Los `_id` de torneos y rondas MongoDB reutilizan como texto el ID correspondiente de PostgreSQL. Los estados se traducen como `NOT_STARTED → PENDIENTE`, `IN_COURSE → IN_COURSE` y `COMPLETED → FINISHED`.

Para revisar que ambos seeders terminaron correctamente:

```bash
docker compose -f infrastructure-archery/docker-compose.yml logs database-archery mongo-replica-set-init
docker compose -f infrastructure-archery/docker-compose.yml exec -T database-archery sh -lc 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT COUNT(*) AS usuarios FROM usuario;"'
docker compose -f infrastructure-archery/docker-compose.yml exec -T mongo-archery mongosh --quiet --eval 'db.getSiblingDB("archerydb").puntuaciones.countDocuments()'
```

Los scripts verifican referencias, totales, posiciones y duplicados antes de declarar la carga como exitosa. PostgreSQL solo ejecuta `/docker-entrypoint-initdb.d` cuando su volumen está vacío; para una reconstrucción completa se debe usar el comando `down -v` anterior.

## Comandos útiles

```bash
docker compose -f infrastructure-archery/docker-compose.yml logs -f
docker compose -f infrastructure-archery/docker-compose.yml ps
docker compose -f infrastructure-archery/docker-compose.yml restart
```

## Variables de entorno

El entorno Docker acepta variables para ajustar puertos, credenciales y zona horaria. Si no defines nada, se usan los valores por defecto del archivo de composición.

Referencia útil:

- `FRONT_PORT` para el frontend
- `BACK_PORT` para el backend
- `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `PGADMIN_PORT`, `PGADMIN_DEFAULT_EMAIL`, `PGADMIN_DEFAULT_PASSWORD`
- `APP_TIMEZONE`

## Qué verás al entrar a la aplicación

### Login

La aplicación inicia en la pantalla de autenticación. Desde ahí el sistema identifica el rol del usuario y redirige a la vista correspondiente.

### Vista de arquero

El arquero accede a su propio perfil, historial y métricas personales. La navegación está limitada a lo que le corresponde ver.
Para acceder a la vista de arquero del sistema, se debe usar un perfil pre-hecho en el sistema,
a modo de ejemplo tenemos las credenciales del siguiente perfil para que pruebes las funcionalidades de este:
    - Rut: 10000001-K
    - Contraseña: arco123

### Vista de administrador

El administrador puede administrar torneos, crear nuevos torneos y revisar el detalle de cada uno para operar sobre rondas, resultados y ranking.
Para acceder a la vista de administrador del sistema, se debe usar un perfil pre-hecho en el sistema,
a modo de ejemplo tenemos las credenciales del siguiente perfil para que pruebes las funcionalidades de este:
    - Rut: 1111111-1
    - Contraseña: admin123

### Leaderboard

La vista de leaderboard permite consultar el rendimiento general y el historial competitivo de los arqueros.


## Cumplimiento de rúbrica según enunciado

### Repositorio y entregables

| Criterio | Ubicación | Estado |
| --- | --- | --- |
| Script de creación de PostgreSQL | `database/sql/01-dbCreate.sql` | ✓ |
| Población de PostgreSQL | `database/sql/11-testData.sql` | ✓ |
| Validadores, índices y población MongoDB | `database/mongo/` | ✓ |
| Archivo README.md con instrucciones | `README.md` (este archivo) | ✓ |

### Aplicación

| Criterio | Descripción | Estado |
| --- | --- | --- |
| Sistema consistente REST + Web + PostgreSQL + MongoDB | Frontend React, Backend Spring Boot y ambas bases en Docker Compose | ✓ |
| CRUD completo para todas las tablas | `ArquerosController`, `TorneoController`, `CategoriaController`, `UsuarioController`, `ParticipacionController`, `RondaController` | ✓ |
| Paginación en recursos | Implementada en endpoints principales | ✓ |
| Queries simples por recurso | Filtering y búsqueda en listados | ✓ |

### Funcionalidades críticas

| Criterio | Implementación | Estado |
| --- | --- | --- |
| Registro de usuario y autenticación JWT | `AuthController` con Spring Security + JWT en cookies HttpOnly | ✓ |
| RBAC (rol-based access control) | Rutas separadas por rol en frontend y filtrado en backend | ✓ |

### SQL avanzado

| Criterio | Descripción | Ubicación | Estado |
| --- | --- | --- | --- |
| Procedimientos almacenados | 2+ procedimientos para registrar puntaje y calcular ranking | `database/sql/04-StoredProced.sql`, `database/sql/05-StoredProced.sql` | ✓ |
| Triggers | Validación de flechas, auditoría y reglas geoespaciales | `database/sql/08-Triggers.sql`, `database/sql/09-Triggers_geo.sql` | ✓ |
| Vista materializada | Leaderboard histórico con top 50 arqueros | `database/sql/02-materialViews.sql` | ✓ |
| Índices | B-Tree y GiST en PostgreSQL; compuestos únicos en MongoDB | `database/sql/`, `database/mongo/02-Indexes.js` | ✓ |

## Notas de implementación

- El backend expone endpoints REST para las entidades principales del dominio.
- La seguridad se apoya en JWT y Spring Security.
- La base de datos concentra reglas críticas para evitar inconsistencias y reforzar el dominio de negocio.
- Los scripts SQL se ejecutan al crear el volumen de PostgreSQL; el inicializador del Replica Set aplica los scripts MongoDB antes de iniciar el backend.

## Estado del proyecto

El sistema está preparado para funcionar de extremo a extremo con Docker Compose, por lo que el flujo estándar es levantar el stack completo y entrar al frontend desde el navegador. El README es suficiente para reproducir todo el proyecto en una máquina limpia.
