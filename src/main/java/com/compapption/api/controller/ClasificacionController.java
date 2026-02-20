package com.compapption.api.controller;

import com.compapption.api.dto.clasificacionDTO.ClasificacionDetalleDTO;
import com.compapption.api.dto.clasificacionDTO.ClasificacionSimpleDTO;
import com.compapption.api.service.ClasificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clasificaciones")
@RequiredArgsConstructor
public class ClasificacionController {

    private final ClasificacionService clasificacionService;

    // Todos los datos de todas las clasificaciones por competición

    @GetMapping("/competiciondetalle/{competicionId}")
    public ResponseEntity<List<ClasificacionDetalleDTO>> obtenerTablaDetalle(
            @PathVariable Long competicionId) {
        return ResponseEntity.ok(clasificacionService.obtenerClasificacionDetalle(competicionId));
    }

    @GetMapping("/competicionsimple/{competicionId}")
    public ResponseEntity<List<ClasificacionSimpleDTO>> obtenerTablaSimple(
            @PathVariable Long competicionId) {
        return ResponseEntity.ok(clasificacionService.obtenerClasificacionSimple(competicionId));
    }

    // Clasificación por temporada de cada competición

    @GetMapping("/competiciondetalle/{competicionId}/temporada/{temporada}")
    public ResponseEntity<List<ClasificacionDetalleDTO>> obtenerPorTemporadaDetalle(
            @PathVariable Long competicionId,
            @PathVariable Integer temporada) {
        return ResponseEntity.ok(clasificacionService.obtenerPorTemporadaDetalle(
                competicionId,
                temporada));
    }

    @GetMapping("/competicionsimple/{competicionId}/temporada/{temporada}")
    public ResponseEntity<List<ClasificacionSimpleDTO>> obtenerPorTemporadaSimple(
            @PathVariable Long competicionId,
            @PathVariable Integer temporada) {
        return ResponseEntity.ok(clasificacionService.obtenerPorTemporadaSimple(
                competicionId,
                temporada));
    }

    // Posición y estadísticas de un equipo

    @GetMapping("/competiciondetalle/{competicionId}/equipo/{equipoId}")
    public ResponseEntity<ClasificacionDetalleDTO> obtenerPorEquipoDetalle(
            @PathVariable Long competicionId,
            @PathVariable Long equipoId) {
        return ResponseEntity.ok(clasificacionService.obtenerPorEquipoDetalle(
                competicionId,
                equipoId));
    }

    @GetMapping("/competicionsimple/{competicionId}/equipo/{equipoId}")
    public ResponseEntity<ClasificacionSimpleDTO> obtenerPorEquipoSimple(
            @PathVariable Long competicionId,
            @PathVariable Long equipoId) {
        return ResponseEntity.ok(clasificacionService.obtenerPorEquipoSimple(
                competicionId,
                equipoId));
    }

    // Recalculo manual de clasificación

    @PostMapping("/competicion/{competicionId}/recalcular")
    public ResponseEntity<List<ClasificacionDetalleDTO>> recalcular(
            @PathVariable Long competicionId
            //@AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        clasificacionService.calcularClasificacion(competicionId);
        return ResponseEntity.ok(
                clasificacionService.obtenerClasificacionDetalle(competicionId));
    }
}
