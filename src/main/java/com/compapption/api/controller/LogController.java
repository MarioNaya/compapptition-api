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

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping("/competicion/{competicionId}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#competicionId, authentication)")
    public ResponseEntity<PageResponse<LogDTO>> obtenerPorCompeticion(
            @PathVariable Long competicionId,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(toPageResponse(logService.obtenerPorCompeticion(competicionId, pageable)));
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN_SISTEMA') or #usuarioId == authentication.principal.id")
    public ResponseEntity<PageResponse<LogDTO>> obtenerPorUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(toPageResponse(logService.obtenerPorUsuario(usuarioId, pageable)));
    }

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
