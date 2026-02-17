package com.compapption.api.controller;

import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.request.equipo.EquipoCreateRequest;
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

@RestController
@RequestMapping("/equipos")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    @GetMapping
    public ResponseEntity<PageResponse<EquipoSimpleDTO>> buscar(
            @RequestParam String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(equipoService.buscar(search, pageable));
    }

    @GetMapping("mis-equipos/manager")
    public ResponseEntity<List<EquipoSimpleDTO>> buscarMisEquiposManager(long id) {
        return ResponseEntity.ok(equipoService.obtenerPorManager(id));
    }

    @GetMapping("mis-equipos/jugador")
    public ResponseEntity<List<EquipoSimpleDTO>> buscarMisEquiposJugador(long id) {
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
}
