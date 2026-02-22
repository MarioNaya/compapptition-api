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

@RestController
@RequestMapping("/competiciones/{competicionId}/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    /// === GETS EVENTOS === ///

    @GetMapping("/simple")
    public ResponseEntity<List<EventoSimpleDTO>> listarSimple(
            @PathVariable Long competicionId){
        return ResponseEntity.ok(eventoService.obtenerPorCompeticionSimple(competicionId));
    }

    @GetMapping("/detalle")
    public ResponseEntity<List<EventoDetalleDTO>> listarDetalle(
            @PathVariable Long competicionId){
        return ResponseEntity.ok(eventoService.obtenerPorCompeticionDetalle(competicionId));
    }

    @GetMapping("/jornada/{jornada}/simple")
    public ResponseEntity<List<EventoSimpleDTO>> listarPorJornadaSimple(
            @PathVariable Long competicionId,
            @PathVariable Integer jornada) {
        return ResponseEntity.ok(eventoService.obtenerPorJornadaSimple(competicionId,jornada));
    }

    @GetMapping("/jornada/{jornada}/detalle")
    public ResponseEntity<List<EventoDetalleDTO>> listarPorJornadaDetalle(
            @PathVariable Long competicionId,
            @PathVariable Integer jornada) {
        return ResponseEntity.ok(eventoService.obtenerPorJornadaDetalle(competicionId,jornada));
    }

    @GetMapping("/calendariosimple")
    public ResponseEntity<List<EventoSimpleDTO>> obtenerPorFechas(
            @PathVariable Long competicionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return  ResponseEntity.ok(eventoService.obtenerPorRangoFechasSimple(competicionId, inicio, fin));
    }

    @GetMapping("/calendariodetalle")
    public ResponseEntity<List<EventoDetalleDTO>> obtenerPorFechasDetalle(
            @PathVariable Long competicionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(eventoService.obtenerPorRangoFechasDetalle(competicionId, inicio, fin));
    }

    @GetMapping("/equipo/{equipoId}")
    public ResponseEntity<List<EventoSimpleDTO>> listarPorEquipoDetalle(
            @PathVariable Long competicionId,
            @PathVariable Long equipoId) {
        return ResponseEntity.ok(eventoService.obtenerPorCompeticionYEquipo(
                competicionId,
                equipoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDetalleDTO> obtenerPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(eventoService.obtenerPorIdDetalle(id));
    }

    /// === POST/PUT EVENTOS === ///

    @PostMapping
    public ResponseEntity<EventoDetalleDTO> crear(
            @PathVariable Long competicionId,
            @Valid @RequestBody EventoCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventoService.crear(competicionId, request));
    }

    @PostMapping("/{id}/resultado")
    public ResponseEntity<EventoResultadoDTO> registrarResultado(
            @PathVariable Long id,
            @Valid @RequestBody ResultadoRequest request) {
        return ResponseEntity.ok(eventoService.registrarResultado(id,request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoDetalleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoUpdateRequest request) {
        return ResponseEntity.ok(eventoService.actualizar(id, request));
    }

    /// === BORRAR EVENTO === ///

    @DeleteMapping("{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id){
        eventoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /// === ENDPOINTS DE ESTADÍSTICAS === ///

    @GetMapping("{id}/estadisticas")
    public ResponseEntity<List<EstadisticaJugadorDTO>> obtenerEstadisticas(
            @PathVariable long id) {
        return ResponseEntity.ok(eventoService.obtenerEstadisticas(id));
    }

    @PostMapping("{id}/estadisticas")
    public ResponseEntity<EstadisticaJugadorDTO> registrarEstadistica(
            @PathVariable Long id,
            @Valid @RequestBody EstadisticaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventoService.registrarEstadistica(id, request));
    }

    @DeleteMapping("/{id}/estadisticas/{estadisticaId}")
    public ResponseEntity<Void> eliminarEstadistica(
            @PathVariable Long id,
            @PathVariable Long estadisticaId) {
        eventoService.eliminarEstadistica(id, estadisticaId);
        return ResponseEntity.noContent().build();
    }
}
