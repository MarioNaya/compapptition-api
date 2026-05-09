# Compapptition · API

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-JJWT_0.12.6-000000?logo=jsonwebtokens&logoColor=white)](https://github.com/jwtk/jjwt)
[![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-FF9E0F)](https://mapstruct.org/)
[![Tests](https://img.shields.io/badge/JUnit-200%2F200-brightgreen)](#tests)
[![License](https://img.shields.io/badge/license-academic-blue)](#licencia)

Backend REST de **Compapptition**, app para la **gestión integral de competiciones deportivas**: ligas, playoffs y formatos mixtos, inscripción de equipos y jugadores, generación automática de calendarios, registro de resultados y estadísticas, clasificaciones en vivo, mensajería 1-a-1, notificaciones SSE, tickets de soporte y RBAC contextual por competición.

Construido con **Spring Boot 4.0.2 + Java 21** sobre MySQL, autenticación JWT (access + refresh cookie HttpOnly), single-flight refresh, almacenamiento de imágenes en **Cloudinary** (server-side autenticado con validación de magic bytes), envío de emails SMTP y suite de tests **200 / 200** (unit + integración + Postman).

> Repositorio de código del Trabajo de Fin de Grado del autor. La documentación pública (memoria, manual técnico, decisiones D01-D41, auditorías) vive en el repo separado **[`compapptition-docs`](https://github.com/MarioNaya/compapptition-docs)**.

---

## Tabla de contenidos

- [Características](#características)
- [Stack técnico](#stack-técnico)
- [Arquitectura](#arquitectura)
- [Modelo de dominio](#modelo-de-dominio)
- [API · áreas y endpoints](#api--áreas-y-endpoints)
- [Seguridad](#seguridad)
- [Requisitos previos](#requisitos-previos)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Base de datos](#base-de-datos)
- [Tests](#tests)
- [Documentación generada (Javadoc)](#documentación-generada-javadoc)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Despliegue](#despliegue)
- [Contexto](#contexto)
- [Licencia](#licencia)

---

## Características

- **Tres formatos de competición** — Liga, Playoff (eliminatoria directa) y Grupos + Playoff. Generadores propios encapsulados (`GeneradorLiga`, `GeneradorPlayoff`, `GeneradorGruposPlayoff`).
- **RBAC contextual por competición** — el mismo usuario puede ser `ADMIN_COMPETICION` en la liga A y `JUGADOR` en la B. Evaluado en runtime con `@PreAuthorize` + `RbacService`.
- **JWT con refresh rotativo en cookie HttpOnly** — access tokens cortos (15 min) en `Authorization: Bearer`, refresh largos (7 días) en cookie `Path=/auth, SameSite=Strict, Secure`, rotación completa en cada `/auth/refresh`, blocklist al logout.
- **Single-flight refresh** — el filtro deduplica refresh tokens en flight para evitar race conditions cuando varios endpoints pillan 401 simultáneamente.
- **Mensajería 1-a-1** — entidades `Conversacion` + `Mensaje`, REST + SSE para entrega en tiempo real (D18).
- **Notificaciones SSE** — `/notificaciones/stream` con fallback `?token=` (porque `EventSource` no soporta headers). Push de eventos: invitaciones, resultados, mensajes, tickets, recordatorio de partido (D18, D36).
- **Tickets de soporte in-app** — los usuarios reportan incidencias desde la app; el admin recibe email + notificación SSE (D37).
- **Imágenes en Cloudinary** — signed upload firmada por el backend (`CloudinaryService`); el frontend nunca ve la API secret. URL String en BD, no `byte[]` (D19).
- **Recordatorio de partido híbrido** — scheduler automático (23h-25h antes) + endpoint manual de "forzar envío" para que el organizador pueda re-disparar la notificación (D36).
- **Tareas programadas** — `InvitacionScheduler` (caduca invitaciones), `NotificacionPartidoScheduler` (recordatorios).
- **Async I/O** — `@Async` en envío de emails (`EmailService`) y escritura de logs de auditoría (`LogAsyncWriter`) con `AsyncConfig` dedicado.
- **Auditoría de modificaciones** — `LogModificacion` persiste quién modificó qué entidad y cuándo.
- **Defense in depth** (Sprint 4) — Bucket4j rate limiting (5-20 req/min en endpoints sensibles), account lockout (5 fallos en 15 min → bloqueo 15 min), password policy (8+ con mayús+dígito), magic-bytes check en uploads, headers HSTS/CSP/X-Frame, CORS fail-fast en perfil prod.

---

## Stack técnico

| Categoría | Tecnología |
|---|---|
| Framework | **Spring Boot 4.0.2** |
| Lenguaje | **Java 21** (LTS) |
| Build | Maven 3.8+ con wrapper `mvnw` |
| Persistencia | Spring Data JPA + Hibernate |
| BD producción | MySQL 8.x |
| BD tests | H2 in-memory + Testcontainers (MySQL real para flujos) |
| DTO ↔ entidad | **MapStruct 1.6.3** |
| Boilerplate | Lombok |
| Seguridad | Spring Security + **JJWT 0.12.6** (HS512) + Bucket4j (rate limit) |
| Validación | Jakarta Validation (`spring-boot-starter-validation`) |
| Email | Spring Mail (SMTP) |
| Imágenes | **Cloudinary** SDK (signed upload) |
| Push tiempo real | Server-Sent Events (`SseEmitter`) |
| Tests unit | JUnit 5 + Mockito + Spring Boot Test |
| Tests integración | Testcontainers (MySQL) + perfil `test` |
| Tests API | Postman + Newman |
| Doc generada | maven-javadoc-plugin 3.10.1 |
| Packaging | JAR ejecutable (Spring Boot Maven Plugin) |

---

## Arquitectura

API REST stateless en capas, con separación estricta DTO / entidad:

```
HTTP request
    │
    ▼
[ Filter chain ]
    ├─ CorsConfigurationSource           CORS antes que Security
    ├─ RateLimitFilter                   Bucket4j (5-20 req/min sensibles)
    ├─ JwtAuthenticatorFilter            valida token + popula SecurityContext
    └─ Spring Security                   stateless, CSRF off, headers HSTS/CSP
    │
    ▼
[ @RestController ]                       @Valid + @PreAuthorize + RbacService
    │
    ▼
[ @Service @Transactional ]               lógica de negocio
    │
    ▼
[ Spring Data JPA ]                       repos + projections + JOIN FETCH
    │
    ▼
[ MySQL 8 ]
```

**Decisiones de diseño relevantes** (ver `docs/50-decisiones/decisiones.md` en el repo de docs):
- **Stateless puro** — sin `HttpSession`. Toda la identidad viaja en el JWT (claims `userId`, `esAdminSistema`, `competiciones[]`).
- **DTOs separados por uso** — `XxxSimpleDTO` para listados, `XxxDetalleDTO` para fichas. Mapeo MapStruct.
- **D18** — Mensajería + notificaciones SSE.
- **D19** — Imágenes URL Cloudinary (no `byte[]` BLOB; despliegue trivial, escalable).
- **D31** — Redacción de credenciales en informes versionados (`<JWT_SECRET_REDACTED>`, `<DB_PASS_REDACTED>`).
- **D37** — Tickets de soporte 0.0.1.
- **D38** — Split de emails legal vs operativa centralizado en `app.support.admin-email`.
- **D40** — Sprint 6 ampliado: Tier 1+2+3 unit + perfil `test` + `TestOnlyController` doble defensa + manual técnico Javadoc.

---

## Modelo de dominio

**25 entidades JPA** agrupadas por contexto:

| Contexto | Entidades |
|---|---|
| **Identidad y acceso** | `Usuario`, `Rol`, `UsuarioRolCompeticion`, `RefreshToken`, `PasswordResetToken` |
| **Competición** | `Competicion`, `ConfiguracionCompeticion`, `CompeticionEquipo`, `Clasificacion` |
| **Estructura deportiva** | `Deporte`, `Equipo`, `Jugador`, `EquipoJugador`, `EquipoManager`, `SolicitudVinculacionJugador` |
| **Actividad** | `Evento`, `EventoEquipo`, `EstadisticaJugadorEvento`, `TipoEstadistica` |
| **Comunicación** | `Conversacion`, `Mensaje`, `Notificacion` |
| **Soporte y transversal** | `Ticket` (+ enum `EstadoTicket`), `Invitacion`, `LogModificacion` |

---

## API · áreas y endpoints

**18 controllers REST + 1 sólo-tests** · **105 endpoints vigentes** + 8 reservados-0.0.2 (post P0.4 cleanup).

| Área | Base path | Highlights |
|---|---|---|
| Autenticación | `/auth` | registro, login, refresh, logout, recuperar/resetear password |
| Competiciones | `/competiciones` | CRUD, inscripción equipos, asignación roles, cambio de estado, avance temporada, calendario |
| Equipos | `/equipos` | CRUD, gestión jugadores y managers, público/privado con `codigoInvitacion` |
| Eventos | `/competiciones/{id}/eventos` | CRUD, registro de resultados, estadísticas por evento |
| Clasificaciones | `/clasificaciones` | standings calculados, endpoints públicos |
| Jugadores | `/jugadores` | CRUD, búsqueda, listado por equipo, vinculaciones |
| Estadísticas | `/estadisticas` | CRUD, consultas pivote por jugador/competición |
| Calendario | `/competiciones/{id}/calendario` | generación automática según formato |
| Invitaciones | `/invitaciones` | crear, aceptar/rechazar, listar bandeja |
| Mensajería | `/conversaciones` | bandeja, conversación 1-a-1, envío |
| Notificaciones | `/notificaciones` | listar, marcar leído, **SSE en `/stream`** |
| Tickets soporte | `/tickets` | crear, listar (mis tickets / todos admin), detalle, estado |
| Imágenes | `/imagenes` | firma para Cloudinary signed upload (D19) |
| Deportes | `/deportes` | catálogo (público en lectura) |
| Tipos estadística | `/tipos-estadistica` | configuración global |
| Usuarios | `/usuarios` | perfil, cambio password |
| Logs auditoría | `/logs` | consultas restringidas a admin |
| Vinculación jugadores | `/solicitudes-vinculacion` | flujo doble validación jugador/manager |
| **Sólo tests** | `/test-only` | reset BD entre tests E2E (activo solo bajo perfil `test`) |

Colección Postman canónica en `postman/compapptition-api.postman_collection.json` con assertions por flujo.

---

## Seguridad

### Autenticación JWT
- **HS512** clave simétrica BASE64 ≥ 256 bits, en `JWT_SECRET`.
- **Access token:** 15 min, header `Authorization: Bearer`.
  Claims: `userId`, `esAdminSistema`, `competiciones[]` con `{id, rol}`.
- **Refresh token:** 7 días, persistido como `RefreshToken`, entregado en **cookie `Path=/auth`, `HttpOnly`, `SameSite=Strict`, `Secure` en prod**.
- **Rotación completa** en cada `/auth/refresh`; el anterior se invalida.
- **BCrypt** strength 10 para passwords.
- **Password reset** vía token UUID por email.

### RBAC contextual

Roles evaluados **por competición**, no global. Ejemplo:

```java
@PreAuthorize("@rbacService.isAdminCompeticion(#competicionId, authentication)")
public ResponseEntity<CompeticionDetalleDTO> actualizar(...) { ... }
```

5 roles + flag global: `ADMIN_COMPETICION`, `ARBITRO`, `MANAGER_EQUIPO`, `JUGADOR`, `INVITADO` + `esAdminSistema`.

### Defense in depth (Sprint 4)
- **Rate limit** Bucket4j: 5-20 req/min en `/auth/login`, `/auth/registro`, `/auth/recuperar-password`, `/tickets`.
- **Account lockout:** 5 fallos en 15 min → bloqueo 15 min (campo `intentosFallidos` en `Usuario`).
- **Password policy:** mínimo 8 caracteres, una mayúscula y un dígito.
- **Magic bytes check** en uploads (rechaza extensiones falsificadas).
- **Headers prod:** HSTS, CSP, X-Frame-Options, Referrer-Policy.
- **CORS fail-fast en perfil prod** — `CorsConfig.validateProductionOrigins()` aborta el contexto si los origins permitidos contienen `localhost`/`127.0.0.1`/`0.0.0.0`.

### Auditorías

4 auditorías originales + 6 re-auditorías post-sprint, todas con veredicto 🟢 al cierre. Detalle en `compapptition-docs/docs/60-auditorias/`.

---

## Requisitos previos

- **JDK 21** (Temurin / OpenJDK).
- **MySQL 8.x** (local o remoto).
- **Maven 3.8+** (wrapper `mvnw` incluido).
- **Cuenta SMTP** para envío de emails (Hostinger, Gmail con app-password, etc.).
- **Cuenta Cloudinary** para subida de imágenes (free tier vale).
- Opcional para tests:
  - **Docker** (Testcontainers + perfil `test` E2E).
  - **Newman** (Postman desde CLI).

---

## Configuración

Toda la config sensible viene de variables de entorno cargadas vía `.env` en raíz (gitignored). El repo incluye `.env.example` con todas las claves y placeholders.

### Instalación rápida (tribunal / nueva máquina)

```bash
cp .env.example .env
${EDITOR:-nano} .env

# Generar JWT_SECRET de 64 bytes base64 (HS512 ≥ 256 bits):
openssl rand -base64 64
```

### Variables requeridas

```env
# Base de datos
DB_HOST=localhost
DB_PORT=3306
DB_NAME=compapptition
DB_USER=compapptition_user
DB_PASS=change-me

# JWT
JWT_SECRET=<BASE64_HS512_256_bits_or_more>
JWT_ACCESS_TOKEN_EXPIRATION=900000        # 15 min
JWT_REFRESH_TOKEN_EXPIRATION=604800000    # 7 días

# SMTP
SPRING_MAIL_HOST=smtp.hostinger.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=no-reply@compapptition.com
SPRING_MAIL_PASSWORD=<contraseña-correo>

# Soporte (D38: split legal vs operativa)
SUPPORT_ADMIN_EMAIL=no-reply@compapptition.com

# Frontend (CORS)
FRONTEND_URL=http://localhost:4200

# Cloudinary (D19)
CLOUDINARY_CLOUD_NAME=<tu-cloud-name>
CLOUDINARY_API_KEY=<tu-api-key>
CLOUDINARY_API_SECRET=<tu-api-secret>
CLOUDINARY_FOLDER=compapptition-dev
```

### Notas para el tribunal
- Para evaluar **sin Cloudinary**, dejar las `CLOUDINARY_*` vacías; los endpoints de subida darán error pero el resto de la app funciona.
- Para evaluar **sin SMTP**, dejar `SPRING_MAIL_USERNAME`/`PASSWORD` vacíos; los flujos de recuperación de password y notificaciones email fallan en silencio (logs) sin bloquear el resto.
- En perfil dev `spring.jpa.hibernate.ddl-auto=update` crea las tablas al primer arranque. En prod debe ser `validate`.

---

## Ejecución

### Desarrollo

```bash
./mvnw spring-boot:run
```

Servidor en `http://localhost:8080`.

### Empaquetado y ejecución del JAR

```bash
./mvnw clean package
java -jar target/api-0.0.1-SNAPSHOT.jar
```

### Verify completo (compile + tests + package)

```bash
./mvnw clean verify
```

### Perfiles disponibles

| Perfil | Uso | Activación |
|---|---|---|
| (default) | Desarrollo local con MySQL real, ddl-auto=update | sin flag |
| `test` | Suite E2E del frontend (perfil aislado, BD distinta, `TestOnlyController` activo) | `--spring.profiles.active=test` o `mvn -Dspring-boot.run.profiles=test` |
| `prod` | Producción: ddl-auto=validate, CORS fail-fast, headers HSTS/CSP, secure cookies | `SPRING_PROFILES_ACTIVE=prod` |

---

## Base de datos

### Creación inicial

```sql
CREATE DATABASE compapptition CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'compapptition_user'@'%' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON compapptition.* TO 'compapptition_user'@'%';
FLUSH PRIVILEGES;
```

### Esquema

Generado por Hibernate (`ddl-auto=update` en dev, `validate` en prod). El esquema canónico exportado para producción se genera con `mysqldump --no-data --routines` desde la BD productiva (no se versiona migración por migración; el cambio entre versiones se documenta en `compapptition-docs/CHANGELOG.md`).

---

## Tests

### 1. Unit + integración (JUnit 5) — 200 / 200

```bash
./mvnw test          # solo unit
./mvnw verify        # unit + IT con Testcontainers (requiere Docker)
```

Cubre:
- Servicios (`AuthServiceTest`, `CompeticionServiceTest`, `TicketServiceTest`, `MensajeriaServiceTest`, `NotificacionServiceTest`, generadores de calendario…).
- Mappers MapStruct (`TicketMapperTest`, `EventoMapperTest`…).
- Repositorios y flujos end-to-end con Testcontainers (`FlujoCampeonatoIT`, `FlujoAuthIT`, `FlujoInvitacionIT`).

### 2. Postman / Newman

Colección completa en `postman/compapptition-api.postman_collection.json`:

```bash
cd postman
newman run compapptition-api.postman_collection.json \
  -e compapptition-local.postman_environment.json
```

Reporte HTML opcional con `htmlextra`.

**Pre-requisitos Postman:** servidor en `localhost:8080` y BD inicial cargada.

### 3. Suite E2E del frontend (perfil `test`)

El frontend (`compapptition/front-web`) tiene una suite Playwright que arranca este backend con perfil `test` + Mailpit + BD aislada vía `docker-compose.test.yml`. Detalles en el README del frontend.

---

## Documentación generada (Javadoc)

Documentación técnica HTML del backend:

```bash
./mvnw javadoc:javadoc
```

Salida en `target/reports/apidocs/` (~8 MB / 559 ficheros). Punto de entrada `index.html`.

Configuración del plugin (`pom.xml`):
- Source 21, doclint off, `failOnError=false`.
- Visibilidad: `protected` y superior.
- Window title "Compapptition API · documentación técnica".

**Plan de publicación:** GitHub Pages del propio repo en cada release (`/javadoc/` o rama `gh-pages`). Manual técnico narrado y enlazado desde `compapptition-docs/docs/20-manual-tecnico/`.

Variante empaquetable como JAR (entregable offline al tribunal):

```bash
./mvnw javadoc:jar
# → target/api-0.0.1-SNAPSHOT-javadoc.jar (autocontenido, descomprimir y abrir index.html)
```

---

## Estructura del proyecto

```
api/
├── src/
│   ├── main/
│   │   ├── java/com/compapptition/api/
│   │   │   ├── config/             # Security, CORS, JWT, Async, RateLimit, DataInitializer
│   │   │   ├── controller/         # 18 REST controllers + TestOnlyController
│   │   │   ├── service/            # 22 services + sub-paquetes calendario/, log/
│   │   │   ├── repository/         # Spring Data JPA repos
│   │   │   ├── entity/             # 25 entidades JPA + enum EstadoTicket
│   │   │   ├── dto/                # DTOs (Simple + Detalle por dominio)
│   │   │   ├── request/            # request classes (CrearXxxRequest, ActualizarXxxRequest)
│   │   │   ├── mapper/             # 15 mappers MapStruct
│   │   │   ├── exception/          # excepciones + GlobalExceptionHandler
│   │   │   └── scheduler/          # InvitacionScheduler, NotificacionPartidoScheduler
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-prod.properties
│   │       ├── application-test.properties
│   │       └── banner.txt
│   └── test/
│       └── java/com/compapptition/api/
│           ├── it/                 # Integration tests con Testcontainers
│           ├── mapper/             # Unit tests de mappers
│           ├── service/            # Unit tests de servicios (incluye TicketServiceTest)
│           └── util/               # BaseUnitTest, BaseRepositoryIT, BaseIntegrationTest
├── postman/                        # Colección + environment + guía
├── pom.xml
├── mvnw / mvnw.cmd
├── .env.example
└── docker-compose.test.yml         # MySQL + Mailpit para suite E2E del frontend
```

---

## Despliegue

### JAR standalone

```bash
./mvnw clean package -DskipTests
java -jar target/api-0.0.1-SNAPSHOT.jar
```

Por defecto puerto 8080 (sobreescribible con `--server.port=9090` o `SERVER_PORT`).

### Variables de entorno en producción

Todas las de [Configuración](#configuración) + obligatorias:

- `SPRING_PROFILES_ACTIVE=prod` (activa headers HSTS/CSP, ddl-auto=validate, CORS fail-fast).
- `JWT_SECRET` ≥ 64 bytes BASE64; rotable; rotación → re-login global.
- `JWT_ACCESS_TOKEN_EXPIRATION` 5-15 min en producción.
- `FRONTEND_URL` con HTTPS obligatorio.
- `app.cookie.secure=true` (refresh cookie con `Secure`).

### Recomendaciones operativas
- Reverse proxy (nginx/Apache) con HTTPS terminado delante.
- CORS restringido al dominio prod (no `localhost:*`).
- Backup automático MySQL diario, retención 7 días mínimo.
- Rotar `JWT_SECRET` cada 6 meses (re-deploy + re-login global).
- Monitorización: log del hosting + alertas a `contacto@marionaya.com`.

Detalle completo de despliegue: `compapptition-docs/docs/20-manual-tecnico/08-despliegue.md`.

---

## Contexto

Compapptition es el proyecto de **Trabajo de Fin de Grado** del autor (curso 2025-26). Tres repos:

| Repo | Contenido | Licencia |
|---|---|---|
| **`compapptition/api`** (este) | Backend Spring Boot 4.0.2 + Java 21 + MySQL | Académica |
| [`compapptition/front-web`](https://github.com/MarioNaya/compapptition-front-web) | Frontend web Angular 21.2 + Tailwind 4 | Académica |
| [`compapptition-docs`](https://github.com/MarioNaya/compapptition-docs) | Documentación pública (memoria, manual técnico, decisiones D01-D41, auditorías, sistema agéntico) | CC BY 4.0 (docs) + MIT (plantilla agéntica) |

Para arquitectura completa, decisiones D01-D41, auditorías de seguridad, manual técnico narrado y estado de tests por release, consulta el repo `compapptition-docs`.

---

## Licencia

Proyecto académico. Uso personal, educativo y de evaluación permitido. Cualquier otro uso requiere contacto previo con el autor (`contacto@marionaya.com`).

---

_Última actualización: mayo 2026 · v0.0.1 · Spring Boot 4.0.2 · Java 21 · 200 / 200 tests_
