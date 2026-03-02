# PROYECTO REAL — Compapption API

## Permisos de Claude en este proyecto

Claude puede **leer y escribir** en este proyecto — siguiendo el flujo guía → real.

Para implementar features nuevas: usar el proyecto GUÍA en `C:\Users\mario\.local\bin\proyecto-tfg\`
Solo portar al real código que ya funciona y ha pasado tests en la guía.

---

## Specs Técnicas

- **Spring Boot:** 4.0.2
- **Java:** 21
- **Package raíz:** `com.compapption.api`
- **DB producción:** MySQL | **DB tests:** H2
- **DTO pattern:** Simple + Detalle (dos clases por entidad)
- **Imágenes:** `byte[]` BLOB (campo `foto`/`imagen` + campo `mimeType`)
- **JWT:** JJWT 0.12.6, HS512, stateless

---

## Estado canónico (2 Mar 2026)

- Backend completo: 20 entidades, 13 controllers, ~105 endpoints, JWT + RBAC + Logs ✅
- Tests JUnit: 149 unit + 40 IT = 189 tests, 0 failures ✅
- Siguiente: Fase 3 — Frontend Web

Ver `C:\Users\mario\.local\bin\proyecto-tfg\CLAUDE.md` para inventario completo y estado de fases.

## Para adaptar código del proyecto guía

Usar `/adapt-to-real [Feature]` desde el proyecto guía.
