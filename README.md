# Compapption · API

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-JJWT_0.12.6-000000?logo=jsonwebtokens&logoColor=white)](https://github.com/jwtk/jjwt)
[![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-FF9E0F)](https://mapstruct.org/)
[![Tests](https://img.shields.io/badge/Tests-28_JUnit_%2B_166_Postman-brightgreen)](#tests)
[![TFG](https://img.shields.io/badge/TFG-2026-blue)](#contexto)

Backend REST de **Compapption**, una plataforma para la **gestión integral de competiciones deportivas**: creación y administración de ligas, playoffs y formatos mixtos, inscripción de equipos y jugadores, generación automática de calendarios, registro de resultados y estadísticas, clasificaciones en tiempo real y control granular de permisos por rol y competición.

Construido con **Spring Boot 4.0.2 + Java 21** sobre MySQL, con autenticación JWT (access + refresh), RBAC contextual por competición, sistema de invitaciones con expiración programada, recuperación de contraseña por email, y auditoría de modificaciones.

> Este repositorio es la entrega real del Trabajo de Fin de Grado (TFG) del autor. Ver sección [Contexto](#contexto).

---

## Tabla de contenidos

- [Características](#características)
- [Stack técnico](#stack-técnico)
- [Arquitectura](#arquitectura)
- [Modelo de dominio](#modelo-de-dominio)
- [API · endpoints principales](#api--endpoints-principales)
- [Seguridad](#seguridad)
- [Requisitos previos](#requisitos-previos)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Base de datos](#base-de-datos)
- [Tests](#tests)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Despliegue](#despliegue)
- [Estadísticas del proyecto](#estadísticas-del-proyecto)
- [Contexto](#contexto)
- [Licencia](#licencia)

---

## Características

- **Gestión completa de competiciones** — creación, edición, cambios de estado, avance de temporada. Tres formatos soportados: **Liga**, **Playoff** (eliminatoria directa) y **Grupos + Playoff**.
- **Generación automática de calendarios** — algoritmo que reparte enfrentamientos según formato y configuración (puntos por victoria/empate, ida/vuelta).
- **Equipos, jugadores y managers** — relaciones ricas con tablas puente (`EquipoJugador`, `EquipoManager`) que permiten históricos y roles múltiples.
- **Eventos deportivos con estadísticas** — registro por evento y por jugador, con tipos de estadística configurables (goles, tarjetas, asistencias…).
- **Clasificaciones en vivo** — cálculo derivado de resultados con endpoints públicos para visualización externa.
- **RBAC por competición** — el mismo usuario puede ser `ADMIN_COMPETICION` en una liga y `JUGADOR` en otra. Evaluado en runtime con `@PreAuthorize` + `RbacService`.
- **Autenticación JWT con rotación de refresh token** — access tokens cortos (15 min), refresh tokens de 7 días en cookie HTTP-only, rotación completa en cada refresh.
- **Invitaciones con expiración** — tokens UUID firmados, scheduler que marca como expiradas las no respondidas.
- **Recuperación de contraseña por email** — token temporal vía SMTP (Gmail por defecto).
- **Auditoría de modificaciones** — `LogModificacion` persiste quién modificó qué entidad y cuándo, consumible por endpoints restringidos a admins.
- **CORS multi-origen para desarrollo** — cualquier puerto de `localhost` + URL configurable del frontend de producción.

---

## Stack técnico

| Categoría | Tecnología |
|---|---|
| Framework | Spring Boot **4.0.2** |
| Lenguaje | Java **21** (LTS) |
| Build | Maven 3.8+ (con wrapper `mvnw`) |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | MySQL 8.x |
| Mapeo DTO ↔ entidad | MapStruct **1.6.3** |
| Boilerplate | Lombok **1.18.38** |
| Seguridad | Spring Security + JJWT **0.12.6** (HS512) |
| Validación | Jakarta Validation (`spring-boot-starter-validation`) |
| Email | Spring Mail (SMTP) |
| Testing unitario | JUnit 5 + Spring Boot Test + H2 in-memory |
| Testing integración | Testcontainers **1.20.4** (MySQL) |
| Testing API | Postman + Newman (166 assertions) |
| Packaging | JAR ejecutable (Spring Boot Maven Plugin) |

---

## Arquitectura

API REST stateless estructurada en capas clásicas (controller → service → repository → entity), con separación estricta entre DTOs de exposición y entidades JPA:

```
HTTP request
    │
    ▼
[ Filter chain ]                 CORS → JWT Authentication → Spring Security
    │
    ▼
[ Controller ]                   REST endpoints (@RestController)
    │   validaciones de entrada (@Valid, @PreAuthorize)
    ▼
[ Service ]                      lógica de negocio, @Transactional
    │
    ▼
[ Repository ]                   Spring Data JPA
    │
    ▼
[ MySQL ]
```

**Decisiones de diseño:**
- **Stateless** — sin `HttpSession`; toda la identidad viaja en el JWT.
- **DTOs separados por uso** — `XxxSimpleDTO` para listados, `XxxDetalleDTO` para fichas completas. Mapeo automatizado con MapStruct.
- **Imágenes como `byte[]` BLOB** — escudos de equipo, fotos de jugador y logos de competición persistidos en la propia base de datos (simplifica despliegue; se migrará a storage externo en futuras versiones).
- **Generación de calendario encapsulada** — clases `GeneradorLiga`, `GeneradorPlayoff`, `GeneradorGruposPlayoff` implementan cada formato con lógica pura testeable.
- **Operaciones asíncronas** — `@Async` en envío de emails y escritura de logs de auditoría, con `AsyncConfig` dedicado.
- **Tareas programadas** — `InvitacionScheduler` barre invitaciones caducadas.

---

## Modelo de dominio

20 entidades JPA agrupadas por contexto:

| Contexto | Entidades |
|---|---|
| **Identidad y acceso** | `Usuario`, `Rol`, `UsuarioRolCompeticion`, `RefreshToken`, `PasswordResetToken` |
| **Competición** | `Competicion`, `ConfiguracionCompeticion`, `CompeticionEquipo`, `Clasificacion` |
| **Estructura deportiva** | `Deporte`, `Equipo`, `Jugador`, `EquipoJugador`, `EquipoManager` |
| **Actividad** | `Evento`, `EventoEquipo`, `EstadisticaJugadorEvento`, `TipoEstadistica` |
| **Transversal** | `Invitacion`, `LogModificacion` |

---

## API · endpoints principales

**13 controllers REST · ~110 endpoints.** Colección Postman completa con **166 assertions** en `postman/`.

| Área | Base path | Endpoints | Highlights |
|---|---|---:|---|
| Autenticación | `/auth` | 7 | registro, login, refresh, logout, recuperar/resetear contraseña |
| Competiciones | `/competiciones` | 18 | CRUD, inscripción de equipos, asignación de roles, cambio de estado, avance de temporada |
| Equipos | `/equipos` | 14 | CRUD, gestión de jugadores y managers, inscripciones |
| Eventos | `/competiciones/{id}/eventos` | 16 | CRUD, registro de resultados, estadísticas por evento |
| Clasificaciones | `/clasificaciones` | 8 | standings calculados, endpoints públicos |
| Jugadores | `/jugadores` | 8 | CRUD, búsqueda, listado por equipo |
| Estadísticas | `/estadisticas` | 7 | CRUD, consultas por evento/jugador |
| Calendario | `/competiciones/{id}/calendario` | 3 | generación automática según formato |
| Invitaciones | `/invitaciones` | 7 | crear, aceptar, rechazar, listar |
| Deportes | `/deportes` | 7 | catálogo (público en lectura) |
| Tipos de estadística | `/tipos-estadistica` | 6 | configuración global |
| Usuarios | `/usuarios` | 6 | perfil, cambio de contraseña |
| Logs de auditoría | `/logs` | 4 | consultas por competición/usuario |

**Endpoints públicos (sin auth):** `GET /auth/**`, `GET /clasificaciones/publicas/**`, `GET /deportes/**`.
**Preflight CORS (`OPTIONS`):** permitido en cualquier ruta.

---

## Seguridad

### Autenticación JWT

- **Algoritmo:** HS512 (clave simétrica BASE64).
- **Access token:** 15 minutos por defecto, transportado en `Authorization: Bearer <token>`.
  - Claims personalizados: `userId`, `esAdminSistema`, `competiciones` (array con `{id, nombre, rol}` para cada competición del usuario).
- **Refresh token:** 7 días, persistido como entidad `RefreshToken` y enviado en **cookie HTTP-only**.
  - **Rotación completa** en cada `/auth/refresh`: el anterior se invalida.
- **Password hashing:** BCrypt (strength 10).
- **Password reset:** token UUID temporal entregado por email (SMTP configurable).

### RBAC contextual

Los roles se evalúan **por competición**, no de forma global. Un `ADMIN_COMPETICION` en la liga A puede no tener privilegios en la liga B.

```java
@PreAuthorize("@rbacService.isAdminCompeticion(#competicionId, authentication)")
public ResponseEntity<CompeticionDetalleDTO> actualizar(...) { ... }
```

Roles definidos: `ADMIN_SISTEMA`, `ADMIN_COMPETICION`, `MANAGER_EQUIPO`, `JUGADOR`, `ARBITRO`, `INVITADO`.

### Filtros

1. `CorsConfig` (`@Order(HIGHEST_PRECEDENCE)`) — CORS antes que Spring Security.
2. `JwtAuthenticatorFilter` (`OncePerRequestFilter`) — valida token y puebla `SecurityContext`.
3. `SecurityConfig` — `@EnableWebSecurity` + `@EnableMethodSecurity`, política stateless, CSRF deshabilitado (API REST).

---

## Requisitos previos

- **JDK 21** (Temurin / OpenJDK recomendados).
- **MySQL 8.x** accesible (local o remoto).
- **Maven 3.8+** (el wrapper `mvnw` incluido lo descarga si no está).
- **Cuenta SMTP** para envío de emails (Gmail con app-password funciona out-of-the-box).
- Opcional: **Node.js + Newman** para ejecutar la colección Postman desde CLI.

---

## Configuración

La aplicación se configura íntegramente por **variables de entorno** leídas de un fichero `.env` en la raíz del proyecto (o exportadas al shell).

### Variables requeridas

```env
# Base de datos MySQL
DB_HOST=localhost
DB_PORT=3306
DB_NAME=compapption
DB_USER=compapption_user
DB_PASS=change-me

# JWT
JWT_SECRET=<BASE64_HS512_256_bits_or_more>
JWT_ACCESS_TOKEN_EXPIRATION=900000        # 15 min
JWT_REFRESH_TOKEN_EXPIRATION=604800000    # 7 días

# SMTP (Gmail por defecto)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-correo@gmail.com
MAIL_PASSWORD=<app-password>

# CORS — origen del frontend de producción
FRONTEND_URL=http://localhost:4200
```

> Generar un `JWT_SECRET` válido:
> ```bash
> openssl rand -base64 64
> ```

### Configuración adicional

Ver `src/main/resources/application.properties` para:
- Política `spring.jpa.hibernate.ddl-auto=update` (actualización automática del esquema).
- Nivel de logs (`compapption=DEBUG` en desarrollo).
- Zona horaria Jackson (`UTC`).

---

## Ejecución

### Desarrollo

```bash
./mvnw spring-boot:run
```

La aplicación arranca en `http://localhost:8080`.

### Compilación y empaquetado

```bash
./mvnw clean package
java -jar target/api-0.0.1-SNAPSHOT.jar
```

### Verificación completa (compile + tests + package)

```bash
./mvnw clean verify
```

---

## Base de datos

### Creación manual

```sql
CREATE DATABASE compapption CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'compapption_user'@'%' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON compapption.* TO 'compapption_user'@'%';
FLUSH PRIVILEGES;
```

### Esquema

Generado automáticamente por Hibernate a partir de las entidades JPA (`ddl-auto=update`). No se usa Liquibase/Flyway en esta versión.

### Datos de ejemplo

```bash
mysql -u compapption_user -p compapption < datos_ejemplo.sql
```

Carga usuarios, roles, deportes, equipos, competiciones, eventos y estadísticas representativos.

Adicionalmente, `test-data/` contiene fixtures JSON (`competiciones.json`, `equipos.json`, `eventos.json`, `flujo-completo.json`, `jugadores.json`) listos para importar vía los endpoints REST o Postman.

---

## Tests

La suite combina **tres niveles de testing** complementarios:

### 1. Unit tests (JUnit 5)

20 clases de tests unitarios sobre servicios y mappers:

```bash
./mvnw test
```

Cubre lógica pura de servicios (`AuthServiceTest`, `CompeticionServiceTest`, `EquipoServiceTest`…), mappers MapStruct y los generadores de calendario (`GeneradorLigaTest`, `GeneradorPlayoffTest`, `GeneradorGruposPlayoffTest`).

### 2. Integration tests (Testcontainers)

8 clases `*IT` que levantan un MySQL real en Docker vía Testcontainers:

- Repositorios (`ClasificacionRepositoryIT`, `CompeticionRepositoryIT`, `EventoRepositoryIT`, `UsuarioRolCompeticionRepositoryIT`).
- Flujos end-to-end (`FlujoAuthIT`, `FlujoCampeonatoIT`, `FlujoInvitacionIT`).

```bash
./mvnw verify
```

> Requiere Docker en ejecución.

### 3. Postman / Newman (166 assertions)

Colección completa en `postman/compapption-api.postman_collection.json` con cobertura de todos los flujos de la API.

```bash
cd postman
newman run compapption-api.postman_collection.json \
  -e compapption-local.postman_environment.json
```

Informe HTML opcional:
```bash
newman run compapption-api.postman_collection.json \
  -e compapption-local.postman_environment.json \
  -r htmlextra --reporter-htmlextra-export informe.html
```

**Pre-requisitos Postman:** servidor corriendo en `localhost:8080` y base de datos con `datos_ejemplo.sql` aplicado.

---

## Estructura del proyecto

```
api/
├── src/
│   ├── main/
│   │   ├── java/com/compapption/api/
│   │   │   ├── config/            # Security, CORS, JWT, Async, DataInitializer
│   │   │   ├── controller/        # 13 REST controllers
│   │   │   ├── service/           # 19 services (incl. log/, calendario/)
│   │   │   ├── repository/        # 20 Spring Data repos
│   │   │   ├── entity/            # 20 entidades JPA
│   │   │   ├── dto/               # 31 DTOs (Simple + Detalle por dominio)
│   │   │   ├── request/           # 16 clases de request
│   │   │   ├── mapper/            # 12 mappers MapStruct
│   │   │   ├── exception/         # 4 excepciones + GlobalExceptionHandler
│   │   │   └── scheduler/         # tareas programadas
│   │   └── resources/
│   │       ├── application.properties
│   │       └── banner.txt
│   └── test/
│       ├── java/com/compapption/api/
│       │   ├── it/                # Integration tests con Testcontainers
│       │   ├── mapper/            # Unit tests de mappers
│       │   ├── service/           # Unit tests de servicios
│       │   └── util/              # Bases: BaseUnitTest, BaseRepositoryIT, BaseIntegrationTest
│       └── resources/
│           └── application-test.properties
├── postman/                       # Colección + environment + guía
├── test-data/                     # Fixtures JSON
├── datos_ejemplo.sql              # Seed SQL
├── pom.xml
└── mvnw / mvnw.cmd                # Maven wrapper
```

---

## Despliegue

### JAR standalone

```bash
./mvnw clean package -DskipTests
java -jar target/api-0.0.1-SNAPSHOT.jar
```

Por defecto escucha en el puerto **8080**. Sobreescribible con `--server.port=9090` o variable `SERVER_PORT`.

### Variables de entorno en producción

Todas las variables de [Configuración](#configuración) son obligatorias. Recomendaciones:

- `JWT_SECRET`: mínimo 64 bytes aleatorios BASE64, rotable.
- `JWT_ACCESS_TOKEN_EXPIRATION`: valorar reducirlo a 5–10 min en entornos sensibles.
- `FRONTEND_URL`: HTTPS obligatorio en producción.
- `spring.jpa.hibernate.ddl-auto`: cambiar a `validate` o `none` tras el primer arranque; gestionar migraciones con Flyway/Liquibase en versiones futuras.

### Consideraciones de seguridad para prod

- Cookie de refresh token: establecer `secure=true` (actualmente `false` para desarrollo local sin HTTPS).
- Reverse proxy (nginx/Traefik) con HTTPS terminado delante.
- CORS: restringir `FRONTEND_URL` al origen de producción únicamente; eliminar la coincidencia con `localhost:*` si no se necesita.

---

## Estadísticas del proyecto

| Métrica | Valor |
|---|---:|
| Líneas de código Java (main + test) | **13.694** |
| Ficheros Java | **151** |
| Entidades JPA | **20** |
| Repositorios | **20** |
| DTOs | **31** |
| Request classes | **16** |
| Services | **19** |
| Mappers (MapStruct) | **12** |
| Controllers REST | **13** |
| Endpoints REST | **~110** |
| Roles definidos | **6** |
| Tests JUnit | **28** (20 unit + 8 integración) |
| Postman assertions | **166** |
| Excepciones custom + handler global | **4 + 1** |

---

## Contexto

Compapption es el proyecto de **Trabajo de Fin de Grado (TFG)** del autor, desarrollado durante el curso 2025-26. El dominio — gestión de competiciones deportivas — se eligió por su riqueza de relaciones, variedad de formatos y cercanía a problemas reales de ligas amateur.

El proyecto completo consta de:

- **API backend** — este repositorio.
- **Frontend web** — Angular 19 (standalone + signals) con Angular Material. Repositorio separado.
- **Frontend mobile** — Ionic 8 + Angular. Roadmap posterior.

Hitos técnicos destacables para la memoria del TFG:

- Arquitectura en capas limpia con separación DTO/entidad estricta.
- Algoritmos propios de generación de calendarios para tres formatos de competición.
- RBAC contextual (no solo por rol global, sino por rol-en-competición).
- Seguridad JWT con rotación de refresh token y recuperación de contraseña por email.
- Cobertura de test combinada unit + integration (Testcontainers) + Postman, con 166 assertions funcionales.

---

## Licencia

Proyecto académico. Uso personal, educativo y de evaluación permitido.
Cualquier otro uso requiere contacto previo con el autor.

---

_Última actualización: abril 2026 · Spring Boot 4.0.2 · Java 21_
