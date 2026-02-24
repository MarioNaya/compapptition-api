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

@RestController
@RequestMapping("/tipos-estadistica")
@RequiredArgsConstructor
public class TipoEstadisticaController {

    private final TipoEstadisticaService tipoEstadisticaService;

    @GetMapping("/deporte/{deporteId}")
    public ResponseEntity<List<TipoEstadisticaDTO>> listarPorDeporte(@PathVariable Long deporteId) {
        return ResponseEntity.ok(tipoEstadisticaService.obtenerPorDeporte(deporteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoEstadisticaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoEstadisticaService.obtenerPorId(id));
    }

    @PostMapping("/deporte/{deporteId}")
    public ResponseEntity<TipoEstadisticaDTO> crear(
            @PathVariable Long deporteId,
            @Valid @RequestBody TipoEstadisticaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tipoEstadisticaService.crear(deporteId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoEstadisticaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoEstadisticaRequest request) {
        return ResponseEntity.ok(tipoEstadisticaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tipoEstadisticaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
