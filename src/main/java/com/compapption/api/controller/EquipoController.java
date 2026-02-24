package com.compapption.api.controller;

import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorDetalleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.request.equipo.EquipoCreateRequest;
import com.compapption.api.request.equipo.EquipoUpdateRequest;
import com.compapption.api.request.page.PageResponse;
import com.compapption.api.service.EquipoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/equipos")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    /// === END POINTS CRUD === ///

    @GetMapping
    public ResponseEntity<PageResponse<EquipoSimpleDTO>> buscar(
            @RequestParam String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(equipoService.buscar(search, pageable));
    }

    @GetMapping("mis-equipos/manager")
    public ResponseEntity<List<EquipoSimpleDTO>> buscarMisEquiposManager(@RequestParam long id) {
        return ResponseEntity.ok(equipoService.obtenerPorManager(id));
    }

    @GetMapping("mis-equipos/jugador")
    public ResponseEntity<List<EquipoSimpleDTO>> buscarMisEquiposJugador(@RequestParam long id) {
        return ResponseEntity.ok(equipoService.obtenerPorJugador(id));
    }

    @GetMapping("/{id}/simple")
    public ResponseEntity<EquipoSimpleDTO> buscarPorIdSimple(@PathVariable long id) {
        return ResponseEntity.ok(equipoService.obtenerPorIdSimple(id));
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<EquipoDetalleDTO> buscarPorIdDetalle(@PathVariable long id) {
        return ResponseEntity.ok(equipoService.obtenerPorIdDetalle(id));
    }

    @PostMapping
    public ResponseEntity<EquipoDetalleDTO> crear(@Valid @RequestBody EquipoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.crear(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<EquipoSimpleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EquipoUpdateRequest request) {
        return ResponseEntity.ok(equipoService.actualizar(id,request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        equipoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /// === END POINTS GESTIÓN PLANTILLA === ///

    @GetMapping("/{id}/jugadores-simple")
    public ResponseEntity<List<JugadorSimpleDTO>> listarJugadoresSimple(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.obtenerJugadoresSimple(id));
    }

    @GetMapping("/{id}/jugadores-detalle")
    public ResponseEntity<List<JugadorDetalleDTO>> listarJugadoresDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.obtenerJugadoresDetalle(id));
    }

    @PostMapping("/{id}/jugadores/{jugadorId}")
    public ResponseEntity<Map<String,String>> agregarJugador(
            @PathVariable Long id,
            @PathVariable Long jugadorId,
            @RequestParam(required = false) Integer dorsal) {
        equipoService.agregarJugador(id,jugadorId,dorsal);
        return ResponseEntity.ok(Map.of("message","Jugador inscrito en el equipo"));
    }

    @DeleteMapping("{id}/jugadores/{jugadorId}")
    public ResponseEntity<Void> quitarJugador(
            @PathVariable Long id,
            @PathVariable Long jugadorId) {
        equipoService.quitarJugador(id, jugadorId);
        return ResponseEntity.noContent().build();
    }

    /// === AÑADIR MANAGER === ///

    @PostMapping("/{id}/managers")
    public ResponseEntity<Map<String,String>> asignarManager(
            @PathVariable Long id,
            @RequestParam Long competicionId,
            @RequestParam Long usuarioId) {
        equipoService.asignarManager(id, competicionId, usuarioId);
        return ResponseEntity.ok(Map.of("message","Manager asignado correctamente"));
    }
}