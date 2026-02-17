package com.compapption.api.controller;

import com.compapption.api.dto.jugadorDTO.JugadorDetalleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorUsuarioDTO;
import com.compapption.api.request.jugador.JugadorCreateRequest;
import com.compapption.api.request.jugador.JugadorUpdateRequest;
import com.compapption.api.request.page.PageResponse;
import com.compapption.api.service.JugadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jugadores")
@RequiredArgsConstructor
public class JugadorController {

    private final JugadorService jugadorService;

    @GetMapping("/buscar")
    public ResponseEntity<PageResponse<JugadorSimpleDTO>> buscar(
            @RequestParam String search,
            @PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(jugadorService.buscar(search, pageable));
    }

    @GetMapping("/detalle/{id}")
    public ResponseEntity<JugadorDetalleDTO> obtenerPorIdDetalle(@PathVariable Long id){
        return ResponseEntity.ok(jugadorService.obtenerPorIdDetalle(id));
    }

    @GetMapping("/simple/{id}")
    public ResponseEntity<JugadorSimpleDTO> obtenerPorIdSimple(@PathVariable Long id){
        return ResponseEntity.ok(jugadorService.obtenerPorIdSimple(id));
    }

    @PostMapping
    public ResponseEntity<JugadorDetalleDTO> crear(
            @Valid @RequestBody JugadorCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(jugadorService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JugadorDetalleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody JugadorUpdateRequest request){
        return ResponseEntity.ok(jugadorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        jugadorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/vincular/{usuarioId}")
    public ResponseEntity<JugadorUsuarioDTO> vincularUsuario(
            @PathVariable Long id,
            @PathVariable Long usuarioId){
        return ResponseEntity.ok(jugadorService.vincularUsuario(id,usuarioId));
    }
}
