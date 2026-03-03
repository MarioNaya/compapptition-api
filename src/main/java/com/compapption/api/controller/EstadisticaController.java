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

/**
 * Controlador REST para la consulta de estadísticas individuales de jugadores.
 * Expone endpoints bajo la ruta base /estadisticas.
 * Gestiona la consulta de estadísticas por jugador, por temporada, por evento,
 * por competición y el ranking acumulado dentro de una competición.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/estadisticas")
@RequiredArgsConstructor
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    /**
     * GET /estadisticas/jugador/{jugadorId} — obtiene todas las estadísticas históricas de un jugador.
     *
     * @param jugadorId identificador único del jugador
     * @return ResponseEntity con la lista de EstadisticaJugadorDTO del jugador
     */
    @GetMapping("/jugador/{jugadorId}")
    public ResponseEntity<List<EstadisticaJugadorDTO>> obtenerPorJugador(
            @PathVariable Long jugadorId) {
        return ResponseEntity.ok(estadisticaService.obtenerPorJugador(jugadorId));
    }

    /**
     * GET /estadisticas/jugador/{jugadorId}/temporada/{temporada} — obtiene las estadísticas de un jugador filtradas por temporada.
     *
     * @param jugadorId identificador único del jugador
     * @param temporada número de temporada a filtrar
     * @return ResponseEntity con la lista de EstadisticaJugadorDTO del jugador en esa temporada
     */
    @GetMapping("/jugador/{jugadorId}/temporada/{temporada}")
    public ResponseEntity<List<EstadisticaJugadorDTO>> obtenerPorJugadorEnTemporada(
            @PathVariable Long jugadorId,
            @PathVariable Integer temporada) {
        return ResponseEntity.ok(estadisticaService.obtenerPorJugadorEnTemporada(jugadorId,temporada));
    }

    /**
     * GET /estadisticas/jugador/{eventoId}/jugador/{jugadorId} — obtiene las estadísticas de un jugador en un evento específico.
     *
     * @param eventoId identificador único del evento (partido)
     * @param jugadorId identificador único del jugador
     * @return ResponseEntity con la lista de EstadisticaJugadorDTO del jugador en ese evento
     */
    @GetMapping("/jugador/{eventoId}/jugador/{jugadorId}")
    public ResponseEntity<List<EstadisticaJugadorDTO>> obtenerPorJugadorEnEvento(
            @PathVariable Long eventoId,
            @PathVariable Long jugadorId) {
        return ResponseEntity.ok(estadisticaService.obtenerPorJugadorEnEvento(eventoId,jugadorId));
    }

    /**
     * GET /estadisticas/competicion/{competicionId}/jugador/{jugadorId} — obtiene todas las estadísticas de un jugador en una competición.
     *
     * @param competicionId identificador único de la competición
     * @param jugadorId identificador único del jugador
     * @return ResponseEntity con la lista de EstadisticaJugadorDTO del jugador en esa competición
     */
    @GetMapping("/competicion/{competicionId}/jugador/{jugadorId}")
    public ResponseEntity<List<EstadisticaJugadorDTO>> obtenerPorJugadorEnCompeticion(
            @PathVariable Long competicionId,
            @PathVariable Long jugadorId) {
        return ResponseEntity.ok(
                estadisticaService.obtenerPorJugadorEnCompeticion(competicionId,jugadorId));
    }

    /**
     * GET /estadisticas/competicion/{competicionId}/jugador/{jugadorId}/acumulado — obtiene las estadísticas acumuladas de un jugador en una competición.
     * Devuelve la suma total de cada tipo de estadística para el jugador en la competición indicada.
     *
     * @param competicionId identificador único de la competición
     * @param jugadorId identificador único del jugador
     * @return ResponseEntity con la lista de EstadisticaAcumuladaDTO con los totales por tipo
     */
    @GetMapping("/competicion/{competicionId}/jugador/{jugadorId}/acumulado")
    public ResponseEntity<List<EstadisticaAcumuladaDTO>> obtenerAcumulado(
            @PathVariable Long competicionId,
            @PathVariable Long jugadorId) {
        return ResponseEntity.ok(
                estadisticaService.obtenerAcumuladoEnCompeticion(competicionId,jugadorId));
    }

    /**
     * GET /estadisticas/competicion/{competicionId}/ranking/{tipoEstadisticaId} — obtiene el ranking de jugadores por un tipo de estadística en una competición.
     * Los resultados se devuelven ordenados de mayor a menor valor acumulado.
     *
     * @param competicionId identificador único de la competición
     * @param tipoEstadisticaId identificador del tipo de estadística (goles, asistencias, etc.)
     * @return ResponseEntity con la lista de EstadisticaAcumuladaDTO ordenada por valor descendente
     */
    @GetMapping("/competicion/{competicionId}/ranking/{tipoEstadisticaId}")
    public ResponseEntity<List<EstadisticaAcumuladaDTO>> obtenerRanking(
            @PathVariable Long competicionId,
            @PathVariable Long tipoEstadisticaId) {
        return ResponseEntity.ok(
                estadisticaService.obtenerRankingEnCompeticion(competicionId,tipoEstadisticaId));
    }




}
