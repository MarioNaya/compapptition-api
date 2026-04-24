package com.compapption.api.controller;

import com.compapption.api.dto.log.LogDTO;
import com.compapption.api.request.page.PageResponse;
import com.compapption.api.service.log.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la consulta de logs de auditoría. Expone endpoints bajo la ruta base /logs.
 * Gestiona la consulta paginada de los registros de modificación generados de forma asíncrona,
 * filtrados por competición, usuario o entidad concreta.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    /**
     * GET /logs/competicion/{competicionId} — obtiene los logs de auditoría de una competición con paginación.
     * Requiere rol de administrador de la competición (verificado via RBAC).
     * Los resultados se devuelven ordenados por fecha descendente por defecto.
     *
     * @param competicionId identificador único de la competición
     * @param pageable parámetros de paginación (por defecto 20 por página, orden por fecha DESC)
     * @return ResponseEntity con una página de LogDTO de la competición indicada
     */
    @GetMapping("/competicion/{competicionId}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#competicionId, authentication)")
    public ResponseEntity<PageResponse<LogDTO>> obtenerPorCompeticion(
            @PathVariable Long competicionId,
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(toPageResponse(logService.obtenerPorCompeticion(competicionId, pageable)));
    }

    /**
     * GET /logs/usuario/{usuarioId} — obtiene los logs de auditoría generados por un usuario con paginación.
     * Solo accesible por el propio usuario o por un administrador del sistema.
     *
     * @param usuarioId identificador único del usuario cuyos logs se consultan
     * @param pageable parámetros de paginación (por defecto 20 por página, orden por fecha DESC)
     * @return ResponseEntity con una página de LogDTO del usuario indicado
     */
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN_SISTEMA') or #usuarioId == authentication.principal.id")
    public ResponseEntity<PageResponse<LogDTO>> obtenerPorUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(toPageResponse(logService.obtenerPorUsuario(usuarioId, pageable)));
    }

    /**
     * GET /logs/entidad/{entidad}/{entidadId} — obtiene todos los logs de auditoría de una entidad concreta.
     * Requiere que el usuario esté autenticado.
     *
     * @param entidad nombre de la entidad de dominio (p.ej. "Competicion", "Equipo", "Evento")
     * @param entidadId identificador único del registro de esa entidad
     * @return ResponseEntity con la lista de LogDTO de la entidad indicada
     */
    @GetMapping("/entidad/{entidad}/{entidadId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LogDTO>> obtenerPorEntidad(
            @PathVariable String entidad,
            @PathVariable Long entidadId) {
        return ResponseEntity.ok(logService.obtenerPorEntidad(entidad, entidadId));
    }

    private PageResponse<LogDTO> toPageResponse(Page<LogDTO> page) {
        return PageResponse.<LogDTO>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
