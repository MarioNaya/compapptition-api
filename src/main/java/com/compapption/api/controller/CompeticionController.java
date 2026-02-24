package com.compapption.api.controller;

import com.compapption.api.dto.competicionDTO.CompeticionDetalleDTO;
import com.compapption.api.dto.competicionDTO.CompeticionInfoDTO;
import com.compapption.api.dto.competicionDTO.CompeticionSimpleDTO;
import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.request.competicion.CompeticionCreateRequest;
import com.compapption.api.request.competicion.CompeticionUpdateRequest;
import com.compapption.api.request.page.PageResponse;
import com.compapption.api.service.CompeticionService;
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
@RequestMapping("/competiciones")
@RequiredArgsConstructor
public class CompeticionController {

    private final CompeticionService competicionService;

    // ==================== CONSULTAS PÚBLICAS ====================

    @GetMapping("/publicas/simple")
    public ResponseEntity<PageResponse<CompeticionSimpleDTO>> listarPublicasSimple(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(competicionService.obtenerPublicas(pageable));
    }

    @GetMapping("/publicas/buscar")
    public ResponseEntity<PageResponse<CompeticionSimpleDTO>> buscarPublicas(
            @RequestParam String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(competicionService.buscarPublicas(search, pageable));
    }

    // ==================== CONSULTAS POR ID ====================

    @GetMapping("/{id}/simple")
    public ResponseEntity<CompeticionSimpleDTO> obtenerSimple(@PathVariable Long id) {
        return ResponseEntity.ok(competicionService.obtenerPorIdSimple(id));
    }

    @GetMapping("/{id}/info")
    public ResponseEntity<CompeticionInfoDTO> obtenerInfo(@PathVariable Long id) {
        return ResponseEntity.ok(competicionService.obtenerPorIdInfo(id));
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<CompeticionDetalleDTO> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(competicionService.obtenerPorIdDetalle(id));
    }

    // ==================== CONSULTAS POR USUARIO ====================

    @GetMapping("/mis-competiciones/creador")
    public ResponseEntity<List<CompeticionSimpleDTO>> obtenerPorCreador(
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(competicionService.obtenerPorCreador(usuarioId));
    }

    @GetMapping("/mis-competiciones/participante")
    public ResponseEntity<List<CompeticionSimpleDTO>> obtenerPorParticipante(
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(competicionService.obtenerPorParticipante(usuarioId));
    }

    // ==================== CRUD ====================

    @PostMapping
    public ResponseEntity<CompeticionDetalleDTO> crear(
            @Valid @RequestBody CompeticionCreateRequest request,
            @RequestParam Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(competicionService.crear(request, usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompeticionDetalleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CompeticionUpdateRequest request,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(competicionService.actualizar(id, request, usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        competicionService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ==================== TEMPORADA ====================

    @PostMapping("/{id}/temporada")
    public ResponseEntity<CompeticionDetalleDTO> cambiarTemporada(
            @PathVariable Long id,
            @RequestParam Integer nuevaTemporada,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(competicionService.cambiarTemporada(id, nuevaTemporada, usuarioId));
    }

    // ==================== GESTIÓN DE EQUIPOS ====================

    @GetMapping("/{id}/equipos/simple")
    public ResponseEntity<List<EquipoSimpleDTO>> listarEquiposSimple(@PathVariable Long id) {
        return ResponseEntity.ok(competicionService.obtenerInscritosSimple(id));
    }

    @GetMapping("/{id}/equipos/detalle")
    public ResponseEntity<List<EquipoDetalleDTO>> listarEquiposDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(competicionService.obtenerInscritosDetalle(id));
    }

    @PostMapping("/{id}/equipos/{equipoId}")
    public ResponseEntity<Map<String, String>> inscribirEquipo(
            @PathVariable Long id,
            @PathVariable Long equipoId,
            @RequestParam Long usuarioId) {
        competicionService.altaEquipo(id, equipoId, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Equipo inscrito en la competición"));
    }

    @DeleteMapping("/{id}/equipos/{equipoId}")
    public ResponseEntity<Void> retirarEquipo(
            @PathVariable Long id,
            @PathVariable Long equipoId,
            @RequestParam Long usuarioId) {
        competicionService.bajaEquipo(id, equipoId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
