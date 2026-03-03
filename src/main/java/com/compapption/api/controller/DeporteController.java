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

/**
 * Controlador REST para la gestión de deportes. Expone endpoints bajo la ruta base /deportes.
 * Las consultas son públicas; las operaciones de creación, actualización y eliminación
 * están restringidas al rol ADMIN_SISTEMA.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/deportes")
@RequiredArgsConstructor
public class DeporteController {

    private final DeporteService deporteService;

    // ==================== CONSULTAS (públicas) ====================

    /**
     * GET /deportes — lista todos los deportes activos en formato resumido.
     *
     * @return ResponseEntity con la lista de DeporteSimpleDTO de los deportes disponibles
     */
    @GetMapping
    public ResponseEntity<List<DeporteSimpleDTO>> listarDeportes() {
        return ResponseEntity.ok(deporteService.obtenerTodosActivosSimple());
    }

    /**
     * GET /deportes/{id} — obtiene los detalles completos de un deporte por su identificador.
     *
     * @param id identificador único del deporte
     * @return ResponseEntity con el DeporteDetalleDTO del deporte solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeporteDetalleDTO> obtenerPorId(@PathVariable long id) {
        return ResponseEntity.ok(deporteService.obtenerPorIdDetalle(id));
    }

    /**
     * GET /deportes/{id}/estadisticas — lista los tipos de estadística definidos para un deporte.
     *
     * @param id identificador único del deporte
     * @return ResponseEntity con la lista de TipoEstadisticaDTO del deporte indicado
     */
    @GetMapping("/{id}/estadisticas")
    public ResponseEntity<List<TipoEstadisticaDTO>> listarEstadisticas(@PathVariable long
                                                                               id) {
        return ResponseEntity.ok(deporteService.obtenerEstadisticasPorDeporte(id));
    }

    // ==================== MUTACIONES (ADMIN_SISTEMA) ====================

    /**
     * POST /deportes — crea un nuevo deporte en el sistema.
     * Restringido al rol ADMIN_SISTEMA.
     *
     * @param request cuerpo con los datos del nuevo deporte (nombre, descripción, escudo)
     * @return ResponseEntity con el DeporteDetalleDTO del deporte creado y estado 201 Created
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public ResponseEntity<DeporteDetalleDTO> crear(@Valid @RequestBody DeporteRequest
                                                           request) {
        return
                ResponseEntity.status(HttpStatus.CREATED).body(deporteService.crear(request));
    }

    /**
     * PUT /deportes/{id} — actualiza los datos de un deporte existente.
     * Restringido al rol ADMIN_SISTEMA.
     *
     * @param id identificador único del deporte a actualizar
     * @param request cuerpo con los nuevos datos del deporte
     * @return ResponseEntity con el DeporteDetalleDTO actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public ResponseEntity<DeporteDetalleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DeporteRequest request) {
        return ResponseEntity.ok(deporteService.actualizar(id, request));
    }

    /**
     * DELETE /deportes/{id} — elimina un deporte del sistema.
     * Restringido al rol ADMIN_SISTEMA.
     *
     * @param id identificador único del deporte a eliminar
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        deporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}