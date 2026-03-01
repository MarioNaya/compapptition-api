package com.compapption.api.controller;
import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.dto.UsuarioRolCompeticion.UsuarioRolCompeticionDTO;
import com.compapption.api.dto.competicionDTO.CompeticionDetalleDTO;
import com.compapption.api.dto.competicionDTO.CompeticionInfoDTO;
import com.compapption.api.dto.competicionDTO.CompeticionSimpleDTO;
import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.entity.Competicion;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CompeticionDetalleDTO competicion = competicionService.crear(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(competicion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompeticionDetalleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CompeticionUpdateRequest request,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(competicionService.actualizar(id, request, usuarioId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        competicionService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ==================== ESTADO ====================

    @PatchMapping("/{id}/estado")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<CompeticionDetalleDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Competicion.EstadoCompeticion estado,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(competicionService.cambiarEstado(id, estado, usuarioId));
    }

    // ==================== TEMPORADA ====================

    @PostMapping("/{id}/temporada")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<CompeticionDetalleDTO> cambiarTemporada(
            @PathVariable Long id,
            @RequestParam Integer nuevaTemporada,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(competicionService.cambiarTemporada(id, nuevaTemporada,
                usuarioId));
    }

    // ==================== GESTIÓN DE EQUIPOS ====================

    @GetMapping("/{id}/equipos/simple")
    public ResponseEntity<List<EquipoSimpleDTO>> listarEquiposSimple(@PathVariable Long id)
    {
        return ResponseEntity.ok(competicionService.obtenerInscritosSimple(id));
    }

    @GetMapping("/{id}/equipos/detalle")
    public ResponseEntity<List<EquipoDetalleDTO>> listarEquiposDetalle(@PathVariable Long
                                                                               id) {
        return ResponseEntity.ok(competicionService.obtenerInscritosDetalle(id));
    }

    @PostMapping("/{id}/equipos/{equipoId}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<Map<String, String>> inscribirEquipo(
            @PathVariable Long id,
            @PathVariable Long equipoId,
            @RequestParam Long usuarioId) {
        competicionService.altaEquipo(id, equipoId, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Equipo inscrito en la competición"));
    }

    @DeleteMapping("/{id}/equipos/{equipoId}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<Void> retirarEquipo(
            @PathVariable Long id,
            @PathVariable Long equipoId,
            @RequestParam Long usuarioId) {
        competicionService.bajaEquipo(id, equipoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ==================== GESTIÓN DE USUARIOS ====================

    @GetMapping("/{id}/usuarios")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<List<UsuarioRolCompeticionDTO>> listarUsuarios(
            @PathVariable Long id) {
        return ResponseEntity.ok(competicionService.obtenerUsuariosConRol(id));
    }

    @DeleteMapping("/{id}/usuarios/{usuarioId}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<Void> quitarUsuario(
            @PathVariable Long id,
            @PathVariable Long usuarioId,
            @RequestParam Long solicitanteId) {
        competicionService.quitarUsuario(id, usuarioId, solicitanteId);
        return ResponseEntity.noContent().build();
    }
}