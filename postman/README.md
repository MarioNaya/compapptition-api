# Colección Postman — Compapption API

## Archivos
- `compapption-api.postman_collection.json` — 84 requests en 11 carpetas
- `compapption-local.postman_environment.json` — Variables del entorno local

## Importar en Postman
1. Importar ambos archivos (File → Import)
2. Seleccionar el environment "Compapption - Local" en la esquina superior derecha

## Ejecutar con Newman (línea de comandos)

### Instalar Newman (una vez)
```bash
npm install -g newman
npm install -g newman-reporter-htmlextra   # informe HTML bonito (opcional)
```

### Ejecutar la colección completa
```bash
cd C:\Users\mario\Desktop\api\postman

# Informe básico en consola
newman run compapption-api.postman_collection.json -e compapption-local.postman_environment.json

# Informe HTML (requiere newman-reporter-htmlextra)
newman run compapption-api.postman_collection.json -e compapption-local.postman_environment.json -r htmlextra --reporter-htmlextra-export informe.html
```

## Variables del environment
| Variable | Valor por defecto | Descripción |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | URL base de la API |
| `usuarioId` | `1` | Usuario admin para endpoints que requieren auth simulada |
| `deporteId` | `1` | Deporte Fútbol (existente en BD) |
| `equipoId` | `1` | Real Madrid CF (existente en BD) |
| `competicionId` | `6` | Liga Test 2026 (2 equipos, LIGA) |
| `playoffCompeticionId` | `8` | Playoff Test 2026 (8 equipos, PLAYOFF) |
| `jugadorId` | `1` | Mario Naya De Luis |
| `tipoEstadisticaId` | `1` | Goles |
| `eventoId` | `8` | Evento finalizado (2-1) en competicion 6 |

## Notas
- Las carpetas siguen orden de dependencia: POST crea el recurso → PUT/DELETE lo usan mediante `{{createdXxxId}}`
- Los endpoints de **Invitaciones** (aceptar/rechazar) requieren un token UUID real; los tests aceptan 200/400/404
- El endpoint **DELETE usuario** apunta al id 999 (inexistente) para no destruir datos de prueba
- La carpeta **Calendario** genera eventos en fechas futuras (2027) para evitar colisiones con datos existentes
