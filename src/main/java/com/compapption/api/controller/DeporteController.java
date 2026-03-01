package com.compapption.api.controller;

import com.compapption.api.dto.deporteDTO.DeporteDetalleDTO;
import com.compapption.api.dto.deporteDTO.DeporteSimpleDTO;
import com.compapption.api.dto.tipoestadisticaDTO.TipoEstadisticaDTO;
import com.compapption.api.request.deporte.DeporteRequest;
import com.compapption.api.service.DeporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deportes")
@RequiredArgsConstructor
public class DeporteController {

    private final DeporteService deporteService;

    // ==================== CONSULTAS (públicas) ====================

    @GetMapping
    public ResponseEntity<List<DeporteSimpleDTO>> listarDeportes() {
        return ResponseEntity.ok(deporteService.obtenerTodosActivosSimple());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeporteDetalleDTO> obtenerPorId(@PathVariable long id) {
        return ResponseEntity.ok(deporteService.obtenerPorIdDetalle(id));
    }

    @GetMapping("/{id}/estadisticas")
    public ResponseEntity<List<TipoEstadisticaDTO>> listarEstadisticas(@PathVariable long
                                                                               id) {
        return ResponseEntity.ok(deporteService.obtenerEstadisticasPorDeporte(id));
    }

    // ==================== MUTACIONES (ADMIN_SISTEMA) ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public ResponseEntity<DeporteDetalleDTO> crear(@Valid @RequestBody DeporteRequest
                                                           request) {
        return
                ResponseEntity.status(HttpStatus.CREATED).body(deporteService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public ResponseEntity<DeporteDetalleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DeporteRequest request) {
        return ResponseEntity.ok(deporteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        deporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}