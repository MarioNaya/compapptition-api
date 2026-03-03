package com.compapption.api.controller;

import com.compapption.api.dto.eventoDTO.EventoDetalleDTO;
import com.compapption.api.request.calendario.CalendarioGenerarRequest;
import com.compapption.api.service.CalendarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la generación de calendarios. Expone endpoints bajo la ruta base
 * /competiciones/{competicionId}/calendario.
 * Gestiona la generación automática de calendarios según el formato de la competición
 * (liga, playoff, grupos+playoff) y la generación del bracket eliminatorio seeded.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/competiciones/{competicionId}/calendario")
@RequiredArgsConstructor
public class CalendarioController {

    private final CalendarioService calendarioService;

    /**
     * POST /competiciones/{competicionId}/calendario — genera el calendario de una competición según su formato.
     * Formatos soportados: LIGA y LIGA_IDA_VUELTA (round-robin), LIGA_PLAYOFF (fase de liga),
     * PLAYOFF (bracket eliminatorio aleatorio), GRUPOS_PLAYOFF (fase de grupos round-robin).
     * El formato EVENTO_UNICO no genera eventos automáticamente.
     *
     * @param competicionId identificador único de la competición
     * @param request cuerpo con la fecha de inicio y los días entre jornadas
     * @return ResponseEntity con la lista de EventoDetalleDTO de los eventos generados
     */
    @PostMapping
    public ResponseEntity<List<EventoDetalleDTO>> generar(
            @PathVariable Long competicionId,
            @Valid @RequestBody CalendarioGenerarRequest request) {
        return ResponseEntity.ok(
                calendarioService.generarCalendarioPorIdDetalle(competicionId, request.getFechaInicio(), request.getDiasJornada()));
    }

    /**
     * POST /competiciones/{competicionId}/calendario/playoff — genera la fase eliminatoria seeded para formatos LIGA_PLAYOFF y GRUPOS_PLAYOFF.
     * Toma los N mejores equipos de la clasificación actual (N = configuracion.numEquiposPlayoff).
     * Si rondaInicial se omite, se calcula automáticamente como maxJornada + 1.
     *
     * @param competicionId identificador único de la competición
     * @param request cuerpo con la fecha de inicio y los días entre rondas
     * @param rondaInicial número de ronda desde la que comenzar el bracket (opcional)
     * @return ResponseEntity con la lista de EventoDetalleDTO del bracket eliminatorio generado
     */
    @PostMapping("/playoff")
    public ResponseEntity<List<EventoDetalleDTO>> generarPlayoff(
            @PathVariable Long competicionId,
            @Valid @RequestBody CalendarioGenerarRequest request,
            @RequestParam(required = false) Integer rondaInicial) {
        return ResponseEntity.ok(
                calendarioService.generarPlayoffSeededPorIdDetalle(
                        competicionId, request.getFechaInicio(), rondaInicial, request.getDiasJornada()));
    }
}
