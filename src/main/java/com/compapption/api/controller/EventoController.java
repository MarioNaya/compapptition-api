package com.compapption.api.controller;

import com.compapption.api.dto.estadisticaDTO.EstadisticaJugadorDTO;
import com.compapption.api.dto.eventoDTO.EventoDetalleDTO;
import com.compapption.api.dto.eventoDTO.EventoResultadoDTO;
import com.compapption.api.dto.eventoDTO.EventoSimpleDTO;
import com.compapption.api.request.estadistica.EstadisticaRequest;
import com.compapption.api.request.evento.EventoCreateRequest;
import com.compapption.api.request.evento.EventoUpdateRequest;
import com.compapption.api.request.evento.ResultadoRequest;
import com.compapption.api.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la gestión de eventos (partidos). Expone endpoints bajo la ruta base
 * /competiciones/{competicionId}/eventos.
 * Gestiona el CRUD de eventos, el registro de resultados, las estadísticas por evento
 * y las consultas por jornada, equipo y rango de fechas.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/competiciones/{competicionId}/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    /// === GETS EVENTOS === ///

    /**
     * GET /competiciones/{competicionId}/eventos/simple — lista todos los eventos de una competición en formato resumido.
     *
     * @param competicionId identificador único de la competición
     * @return ResponseEntity con la lista de EventoSimpleDTO de la competición
     */
    @GetMapping("/simple")
    public ResponseEntity<List<EventoSimpleDTO>> listarSimple(
            @PathVariable Long competicionId){
        return ResponseEntity.ok(eventoService.obtenerPorCompeticionSimple(competicionId));
    }

    /**
     * GET /competiciones/{competicionId}/eventos/detalle — lista todos los eventos de una competición en formato completo.
     *
     * @param competicionId identificador único de la competición
     * @return ResponseEntity con la lista de EventoDetalleDTO de la competición
     */
    @GetMapping("/detalle")
    public ResponseEntity<List<EventoDetalleDTO>> listarDetalle(
            @PathVariable Long competicionId){
        return ResponseEntity.ok(eventoService.obtenerPorCompeticionDetalle(competicionId));
    }

    /**
     * GET /competiciones/{competicionId}/eventos/jornada/{jornada}/simple — lista los eventos de una jornada concreta en formato resumido.
     *
     * @param competicionId identificador único de la competición
     * @param jornada número de jornada o ronda a consultar
     * @return ResponseEntity con la lista de EventoSimpleDTO de esa jornada
     */
    @GetMapping("/jornada/{jornada}/simple")
    public ResponseEntity<List<EventoSimpleDTO>> listarPorJornadaSimple(
            @PathVariable Long competicionId,
            @PathVariable Integer jornada) {
        return ResponseEntity.ok(eventoService.obtenerPorJornadaSimple(competicionId,jornada));
    }

    /**
     * GET /competiciones/{competicionId}/eventos/jornada/{jornada}/detalle — lista los eventos de una jornada concreta en formato completo.
     *
     * @param competicionId identificador único de la competición
     * @param jornada número de jornada o ronda a consultar
     * @return ResponseEntity con la lista de EventoDetalleDTO de esa jornada
     */
    @GetMapping("/jornada/{jornada}/detalle")
    public ResponseEntity<List<EventoDetalleDTO>> listarPorJornadaDetalle(
            @PathVariable Long competicionId,
            @PathVariable Integer jornada) {
        return ResponseEntity.ok(eventoService.obtenerPorJornadaDetalle(competicionId,jornada));
    }

    /**
     * GET /competiciones/{competicionId}/eventos/calendariosimple — obtiene los eventos dentro de un rango de fechas en formato resumido.
     *
     * @param competicionId identificador único de la competición
     * @param inicio fecha y hora de inicio del rango (ISO 8601)
     * @param fin fecha y hora de fin del rango (ISO 8601)
     * @return ResponseEntity con la lista de EventoSimpleDTO en el rango especificado
     */
    @GetMapping("/calendariosimple")
    public ResponseEntity<List<EventoSimpleDTO>> obtenerPorFechas(
            @PathVariable Long competicionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return  ResponseEntity.ok(eventoService.obtenerPorRangoFechasSimple(competicionId, inicio, fin));
    }

    /**
     * GET /competiciones/{competicionId}/eventos/calendariodetalle — obtiene los eventos dentro de un rango de fechas en formato completo.
     *
     * @param competicionId identificador único de la competición
     * @param inicio fecha y hora de inicio del rango (ISO 8601)
     * @param fin fecha y hora de fin del rango (ISO 8601)
     * @return ResponseEntity con la lista de EventoDetalleDTO en el rango especificado
     */
    @GetMapping("/calendariodetalle")
    public ResponseEntity<List<EventoDetalleDTO>> obtenerPorFechasDetalle(
            @PathVariable Long competicionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(eventoService.obtenerPorRangoFechasDetalle(competicionId, inicio, fin));
    }

    /**
     * GET /competiciones/{competicionId}/eventos/equipo/{equipoId} — obtiene todos los eventos en los que participa un equipo.
     *
     * @param competicionId identificador único de la competición
     * @param equipoId identificador del equipo cuyos partidos se consultan
     * @return ResponseEntity con la lista de EventoSimpleDTO del equipo en la competición
     */
    @GetMapping("/equipo/{equipoId}")
    public ResponseEntity<List<EventoSimpleDTO>> listarPorEquipoDetalle(
            @PathVariable Long competicionId,
            @PathVariable Long equipoId) {
        return ResponseEntity.ok(eventoService.obtenerPorCompeticionYEquipo(
                competicionId,
                equipoId));
    }

    /**
     * GET /competiciones/{competicionId}/eventos/{id} — obtiene un evento concreto en formato completo por su identificador.
     *
     * @param id identificador único del evento
     * @return ResponseEntity con el EventoDetalleDTO del evento solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoDetalleDTO> obtenerPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(eventoService.obtenerPorIdDetalle(id));
    }

    /// === POST/PUT EVENTOS === ///

    /**
     * POST /competiciones/{competicionId}/eventos — crea un nuevo evento (partido) en una competición.
     *
     * @param competicionId identificador único de la competición
     * @param request cuerpo con los datos del evento (equipos, fecha, jornada, etc.)
     * @return ResponseEntity con el EventoDetalleDTO del evento creado y estado 201 Created
     */
    @PostMapping
    public ResponseEntity<EventoDetalleDTO> crear(
            @PathVariable Long competicionId,
            @Valid @RequestBody EventoCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventoService.crear(competicionId, request));
    }

    /**
     * POST /competiciones/{competicionId}/eventos/{id}/resultado — registra el resultado de un evento finalizado.
     * Si la competición tiene formato con playoff, procesa automáticamente el avance del ganador.
     *
     * @param id identificador único del evento
     * @param request cuerpo con los marcadores del evento (goles/puntos local y visitante)
     * @return ResponseEntity con el EventoResultadoDTO que refleja el resultado registrado
     */
    @PostMapping("/{id}/resultado")
    public ResponseEntity<EventoResultadoDTO> registrarResultado(
            @PathVariable Long id,
            @Valid @RequestBody ResultadoRequest request) {
        return ResponseEntity.ok(eventoService.registrarResultado(id,request));
    }

    /**
     * PUT /competiciones/{competicionId}/eventos/{id} — actualiza los datos de un evento existente.
     *
     * @param id identificador único del evento a actualizar
     * @param request cuerpo con los nuevos datos del evento (fecha, jornada, equipos, etc.)
     * @return ResponseEntity con el EventoDetalleDTO actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventoDetalleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoUpdateRequest request) {
        return ResponseEntity.ok(eventoService.actualizar(id, request));
    }

    /// === BORRAR EVENTO === ///

    /**
     * DELETE /competiciones/{competicionId}/eventos/{id} — elimina un evento de la competición.
     *
     * @param id identificador único del evento a eliminar
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id){
        eventoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /// === ENDPOINTS DE ESTADÍSTICAS === ///

    /**
     * GET /competiciones/{competicionId}/eventos/{id}/estadisticas — obtiene todas las estadísticas registradas en un evento.
     *
     * @param id identificador único del evento
     * @return ResponseEntity con la lista de EstadisticaJugadorDTO del evento
     */
    @GetMapping("{id}/estadisticas")
    public ResponseEntity<List<EstadisticaJugadorDTO>> obtenerEstadisticas(
            @PathVariable long id) {
        return ResponseEntity.ok(eventoService.obtenerEstadisticas(id));
    }

    /**
     * POST /competiciones/{competicionId}/eventos/{id}/estadisticas — registra una estadística de un jugador en un evento.
     *
     * @param id identificador único del evento
     * @param request cuerpo con los datos de la estadística (jugador, tipo, valor)
     * @return ResponseEntity con el EstadisticaJugadorDTO registrado y estado 201 Created
     */
    @PostMapping("{id}/estadisticas")
    public ResponseEntity<EstadisticaJugadorDTO> registrarEstadistica(
            @PathVariable Long id,
            @Valid @RequestBody EstadisticaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventoService.registrarEstadistica(id, request));
    }

    /**
     * DELETE /competiciones/{competicionId}/eventos/{id}/estadisticas/{estadisticaId} — elimina una estadística concreta de un evento.
     *
     * @param id identificador único del evento
     * @param estadisticaId identificador único de la estadística a eliminar
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}/estadisticas/{estadisticaId}")
    public ResponseEntity<Void> eliminarEstadistica(
            @PathVariable Long id,
            @PathVariable Long estadisticaId) {
        eventoService.eliminarEstadistica(id, estadisticaId);
        return ResponseEntity.noContent().build();
    }
}
