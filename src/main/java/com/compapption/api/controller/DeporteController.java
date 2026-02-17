package com.compapption.api.controller;

import com.compapption.api.dto.deporteDTO.DeporteDetalleDTO;
import com.compapption.api.dto.deporteDTO.DeporteSimpleDTO;
import com.compapption.api.dto.tipoestadisticaDTO.TipoEstadisticaDTO;
import com.compapption.api.service.DeporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deportes")
@RequiredArgsConstructor
public class DeporteController {

    private final DeporteService deporteService;

    @GetMapping
    public ResponseEntity<List<DeporteSimpleDTO>> listarDeportes() {
        return ResponseEntity.ok(deporteService.obtenerTodosActivosSimple());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeporteDetalleDTO> obtenerPorId(@PathVariable long id) {
        return ResponseEntity.ok(deporteService.obtenerPorIdDetalle(id));
    }

    @GetMapping("/{id}/estadisticas")
    public ResponseEntity<List<TipoEstadisticaDTO>> listarEstadisticas(@PathVariable long id) {
        return ResponseEntity.ok(deporteService.obtenerEstadisticasPorDeporte(id));
    }
}
