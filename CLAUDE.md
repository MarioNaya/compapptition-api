# PROYECTO REAL — Compapption API

## ⚠️ CRITICAL: Claude MUST NOT write to any files in this project

Este directorio contiene el **proyecto real del TFG** (el que se entrega a la universidad).

**Claude solo puede LEER** — NUNCA crear, editar ni eliminar ficheros aquí.
Solo Mario (el usuario) escribe en este proyecto.

Para implementar features: usar el proyecto GUÍA en `C:\Users\mario\.local\bin\proyecto-tfg\`
Para comparar estado: leer ficheros aquí y reportar diferencias — nunca modificar.

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

## Estado canónico

Ver `C:\Users\mario\.local\bin\proyecto-tfg\CLAUDE.md` para inventario completo y estado de fases.

## Para adaptar código del proyecto guía

Usar `/adapt-to-real [Feature]` desde el proyecto guía.
