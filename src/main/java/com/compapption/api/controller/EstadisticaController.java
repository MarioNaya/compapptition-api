package com.compapption.api.controller;

import com.compapption.api.dto.estadisticaDTO.EstadisticaAcumuladaDTO;
import com.compapption.api.dto.estadisticaDTO.EstadisticaJugadorDTO;
import com.compapption.api.service.EstadisticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/estadisticas")
@RequiredArgsConstructor
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    @GetMapping("/jugador/{jugadorId}")
    public ResponseEntity<List<EstadisticaJugadorDTO>> obtenerPorJugador(
            @PathVariable Long jugadorId) {
        return ResponseEntity.ok(estadisticaService.obtenerPorJugador(jugadorId));
    }

    @GetMapping("/jugador/{jugadorId}/temporada/{temporada}")
    public ResponseEntity<List<EstadisticaJugadorDTO>> obtenerPorJugadorEnTemporada(
            @PathVariable Long jugadorId,
            @PathVariable Integer temporada) {
        return ResponseEntity.ok(estadisticaService.obtenerPorJugadorEnTemporada(jugadorId,temporada));
    }

    @GetMapping("/jugador/{eventoId}/jugador/{jugadorId}")
    public ResponseEntity<List<EstadisticaJugadorDTO>> obtenerPorJugadorEnEvento(
            @PathVariable Long eventoId,
            @PathVariable Long jugadorId) {
        return ResponseEntity.ok(estadisticaService.obtenerPorJugadorEnEvento(eventoId,jugadorId));
    }

    @GetMapping("/competicion/{competicionId}/jugador/{jugadorId}")
    public ResponseEntity<List<EstadisticaJugadorDTO>> obtenerPorJugadorEnCompeticion(
            @PathVariable Long competicionId,
            @PathVariable Long jugadorId) {
        return ResponseEntity.ok(
                estadisticaService.obtenerPorJugadorEnCompeticion(competicionId,jugadorId));
    }

    @GetMapping("/competicion/{competicionId}/jugador/{jugadorId}/acumulado")
    public ResponseEntity<List<EstadisticaAcumuladaDTO>> obtenerAcumulado(
            @PathVariable Long competicionId,
            @PathVariable Long jugadorId) {
        return ResponseEntity.ok(
                estadisticaService.obtenerAcumuladoEnCompeticion(competicionId,jugadorId));
    }

    @GetMapping("/competicion/{competicionId}/ranking/{tipoEstadisticaId}")
    public ResponseEntity<List<EstadisticaAcumuladaDTO>> obtenerRanking(
            @PathVariable Long competicionId,
            @PathVariable Long tipoEstadisticaId) {
        return ResponseEntity.ok(
                estadisticaService.obtenerRankingEnCompeticion(competicionId,tipoEstadisticaId));
    }




}
