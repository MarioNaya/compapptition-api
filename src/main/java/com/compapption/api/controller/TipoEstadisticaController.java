package com.compapption.api.controller;

import com.compapption.api.dto.tipoestadisticaDTO.TipoEstadisticaDTO;
import com.compapption.api.request.tipoestadistica.TipoEstadisticaRequest;
import com.compapption.api.service.TipoEstadisticaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de tipos de estadística. Expone endpoints bajo la ruta base /tipos-estadistica.
 * Gestiona el CRUD de los tipos de estadística (goles, asistencias, rebotes, etc.)
 * asociados a un deporte concreto.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/tipos-estadistica")
@RequiredArgsConstructor
public class TipoEstadisticaController {

    private final TipoEstadisticaService tipoEstadisticaService;

    /**
     * GET /tipos-estadistica/deporte/{deporteId} — lista todos los tipos de estadística disponibles para un deporte.
     *
     * @param deporteId identificador único del deporte
     * @return ResponseEntity con la lista de TipoEstadisticaDTO del deporte indicado
     */
    @GetMapping("/deporte/{deporteId}")
    public ResponseEntity<List<TipoEstadisticaDTO>> listarPorDeporte(@PathVariable Long deporteId) {
        return ResponseEntity.ok(tipoEstadisticaService.obtenerPorDeporte(deporteId));
    }

    /**
     * GET /tipos-estadistica/{id} — obtiene un tipo de estadística por su identificador.
     *
     * @param id identificador único del tipo de estadística
     * @return ResponseEntity con el TipoEstadisticaDTO solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<TipoEstadisticaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoEstadisticaService.obtenerPorId(id));
    }

    /**
     * POST /tipos-estadistica/deporte/{deporteId} — crea un nuevo tipo de estadística asociado a un deporte.
     *
     * @param deporteId identificador único del deporte al que pertenece el tipo de estadística
     * @param request cuerpo con los datos del nuevo tipo (nombre, descripción, unidad de medida)
     * @return ResponseEntity con el TipoEstadisticaDTO creado y estado 201 Created
     */
    @PostMapping("/deporte/{deporteId}")
    public ResponseEntity<TipoEstadisticaDTO> crear(
            @PathVariable Long deporteId,
            @Valid @RequestBody TipoEstadisticaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tipoEstadisticaService.crear(deporteId, request));
    }

    /**
     * PUT /tipos-estadistica/{id} — actualiza los datos de un tipo de estadística existente.
     *
     * @param id identificador único del tipo de estadística a actualizar
     * @param request cuerpo con los nuevos datos del tipo de estadística
     * @return ResponseEntity con el TipoEstadisticaDTO actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<TipoEstadisticaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoEstadisticaRequest request) {
        return ResponseEntity.ok(tipoEstadisticaService.actualizar(id, request));
    }

    /**
     * DELETE /tipos-estadistica/{id} — elimina un tipo de estadística del sistema.
     *
     * @param id identificador único del tipo de estadística a eliminar
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tipoEstadisticaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
