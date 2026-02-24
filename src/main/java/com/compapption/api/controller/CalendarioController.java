package com.compapption.api.controller;

import com.compapption.api.dto.eventoDTO.EventoDetalleDTO;
import com.compapption.api.request.calendario.CalendarioGenerarRequest;
import com.compapption.api.service.CalendarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/competiciones/{competicionId}/calendario")
@RequiredArgsConstructor
public class CalendarioController {

    private final CalendarioService calendarioService;

    /**
     * Genera el calendario completo según el formato de la competición:
     * - LIGA / LIGA_IDA_VUELTA → round-robin
     * - LIGA_PLAYOFF → fase de liga (el playoff se genera después con /playoff)
     * - PLAYOFF → bracket aleatorio
     * - GRUPOS_PLAYOFF → fase de grupos round-robin (el playoff se genera después con /playoff)
     * - EVENTO_UNICO → no genera eventos automáticamente
     */
    @PostMapping
    public ResponseEntity<List<EventoDetalleDTO>> generar(
            @PathVariable Long competicionId,
            @Valid @RequestBody CalendarioGenerarRequest request) {
        return ResponseEntity.ok(
                calendarioService.generarCalendarioPorIdDetalle(competicionId, request.getFechaInicio(), request.getDiasJornada()));
    }

    /**
     * Genera la fase eliminatoria seeded para formatos LIGA_PLAYOFF y GRUPOS_PLAYOFF.
     * Toma los N mejores equipos de la clasificación actual (N = config.numEquiposPlayoff).
     * El parámetro rondaInicial es opcional: si se omite, se calcula como maxJornada + 1.
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
