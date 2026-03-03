package com.compapption.api.controller;

import com.compapption.api.dto.clasificacionDTO.ClasificacionDetalleDTO;
import com.compapption.api.dto.clasificacionDTO.ClasificacionSimpleDTO;
import com.compapption.api.service.ClasificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la consulta de clasificaciones. Expone endpoints bajo la ruta base /clasificaciones.
 * Gestiona la consulta de la tabla de posiciones por competición, por temporada y por equipo,
 * así como el recálculo manual de la clasificación.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/clasificaciones")
@RequiredArgsConstructor
public class ClasificacionController {

    private final ClasificacionService clasificacionService;

    /**
     * GET /clasificaciones/competiciondetalle/{competicionId} — obtiene la tabla de clasificación completa de una competición.
     *
     * @param competicionId identificador único de la competición
     * @return ResponseEntity con la lista de ClasificacionDetalleDTO ordenada por posición
     */
    @GetMapping("/competiciondetalle/{competicionId}")
    public ResponseEntity<List<ClasificacionDetalleDTO>> obtenerTablaDetalle(
            @PathVariable Long competicionId) {
        return ResponseEntity.ok(clasificacionService.obtenerClasificacionDetalle(competicionId));
    }

    /**
     * GET /clasificaciones/competicionsimple/{competicionId} — obtiene la tabla de clasificación resumida de una competición.
     *
     * @param competicionId identificador único de la competición
     * @return ResponseEntity con la lista de ClasificacionSimpleDTO ordenada por posición
     */
    @GetMapping("/competicionsimple/{competicionId}")
    public ResponseEntity<List<ClasificacionSimpleDTO>> obtenerTablaSimple(
            @PathVariable Long competicionId) {
        return ResponseEntity.ok(clasificacionService.obtenerClasificacionSimple(competicionId));
    }

    // Clasificación por temporada de cada competición

    /**
     * GET /clasificaciones/competiciondetalle/{competicionId}/temporada/{temporada} — obtiene la tabla de clasificación de una temporada concreta en formato completo.
     *
     * @param competicionId identificador único de la competición
     * @param temporada número de temporada a consultar
     * @return ResponseEntity con la lista de ClasificacionDetalleDTO de esa temporada
     */
    @GetMapping("/competiciondetalle/{competicionId}/temporada/{temporada}")
    public ResponseEntity<List<ClasificacionDetalleDTO>> obtenerPorTemporadaDetalle(
            @PathVariable Long competicionId,
            @PathVariable Integer temporada) {
        return ResponseEntity.ok(clasificacionService.obtenerPorTemporadaDetalle(
                competicionId,
                temporada));
    }

    /**
     * GET /clasificaciones/competicionsimple/{competicionId}/temporada/{temporada} — obtiene la tabla de clasificación de una temporada concreta en formato resumido.
     *
     * @param competicionId identificador único de la competición
     * @param temporada número de temporada a consultar
     * @return ResponseEntity con la lista de ClasificacionSimpleDTO de esa temporada
     */
    @GetMapping("/competicionsimple/{competicionId}/temporada/{temporada}")
    public ResponseEntity<List<ClasificacionSimpleDTO>> obtenerPorTemporadaSimple(
            @PathVariable Long competicionId,
            @PathVariable Integer temporada) {
        return ResponseEntity.ok(clasificacionService.obtenerPorTemporadaSimple(
                competicionId,
                temporada));
    }

    // Posición y estadísticas de un equipo

    /**
     * GET /clasificaciones/competiciondetalle/{competicionId}/equipo/{equipoId} — obtiene la clasificación de un equipo concreto en formato completo.
     *
     * @param competicionId identificador único de la competición
     * @param equipoId identificador único del equipo
     * @return ResponseEntity con el ClasificacionDetalleDTO del equipo en la competición
     */
    @GetMapping("/competiciondetalle/{competicionId}/equipo/{equipoId}")
    public ResponseEntity<ClasificacionDetalleDTO> obtenerPorEquipoDetalle(
            @PathVariable Long competicionId,
            @PathVariable Long equipoId) {
        return ResponseEntity.ok(clasificacionService.obtenerPorEquipoDetalle(
                competicionId,
                equipoId));
    }

    /**
     * GET /clasificaciones/competicionsimple/{competicionId}/equipo/{equipoId} — obtiene la clasificación de un equipo concreto en formato resumido.
     *
     * @param competicionId identificador único de la competición
     * @param equipoId identificador único del equipo
     * @return ResponseEntity con el ClasificacionSimpleDTO del equipo en la competición
     */
    @GetMapping("/competicionsimple/{competicionId}/equipo/{equipoId}")
    public ResponseEntity<ClasificacionSimpleDTO> obtenerPorEquipoSimple(
            @PathVariable Long competicionId,
            @PathVariable Long equipoId) {
        return ResponseEntity.ok(clasificacionService.obtenerPorEquipoSimple(
                competicionId,
                equipoId));
    }

    // Recalculo manual de clasificación

    /**
     * POST /clasificaciones/competicion/{competicionId}/recalcular — recalcula manualmente la clasificación de una competición.
     * Recorre todos los resultados registrados y recomputa puntos, victorias, derrotas y diferencia de goles.
     *
     * @param competicionId identificador único de la competición a recalcular
     * @return ResponseEntity con la lista de ClasificacionDetalleDTO actualizada tras el recálculo
     */
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
