package com.compapption.api.controller;
import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.dto.UsuarioRolCompeticion.UsuarioRolCompeticionDTO;
import com.compapption.api.dto.competicionDTO.CompeticionDetalleDTO;
import com.compapption.api.dto.competicionDTO.CompeticionInfoDTO;
import com.compapption.api.dto.competicionDTO.CompeticionSimpleDTO;
import com.compapption.api.dto.competicionDTO.MisCompeticionesPorRolDTO;
import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.entity.Competicion;
import com.compapption.api.request.competicion.CompeticionCreateRequest;
import com.compapption.api.request.competicion.CompeticionUpdateRequest;
import com.compapption.api.request.page.PageResponse;
import com.compapption.api.service.CompeticionService;
import com.compapption.api.service.JugadorService;
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

/**
 * Controlador REST para la gestión de competiciones. Expone endpoints bajo la ruta base /competiciones.
 * Gestiona el CRUD de competiciones, la inscripción y retirada de equipos, la administración
 * de usuarios con roles, el cambio de estado y el avance de temporada.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/competiciones")
@RequiredArgsConstructor
public class CompeticionController {

    private final CompeticionService competicionService;
    private final JugadorService jugadorService;

    // ==================== CONSULTAS PÚBLICAS ====================

    /**
     * GET /competiciones/publicas/simple — lista todas las competiciones públicas con paginación.
     *
     * @param pageable parámetros de paginación (por defecto 20 por página)
     * @return ResponseEntity con una página de CompeticionSimpleDTO de competiciones públicas
     */
    @GetMapping("/publicas/simple")
    public ResponseEntity<PageResponse<CompeticionSimpleDTO>> listarPublicasSimple(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(competicionService.obtenerPublicas(pageable));
    }

    /**
     * GET /competiciones/publicas/buscar — busca competiciones públicas por nombre o criterio con paginación.
     *
     * @param search término de búsqueda para filtrar competiciones
     * @param pageable parámetros de paginación (por defecto 20 por página)
     * @return ResponseEntity con una página de CompeticionSimpleDTO que coinciden con la búsqueda
     */
    @GetMapping("/publicas/buscar")
    public ResponseEntity<PageResponse<CompeticionSimpleDTO>> buscarPublicas(
            @RequestParam String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(competicionService.buscarPublicas(search, pageable));
    }

    // ==================== CONSULTAS POR ID ====================

    /**
     * GET /competiciones/{id}/simple — obtiene la vista resumida de una competición por su identificador.
     *
     * @param id identificador único de la competición
     * @return ResponseEntity con el CompeticionSimpleDTO de la competición solicitada
     */
    @GetMapping("/{id}/simple")
    public ResponseEntity<CompeticionSimpleDTO> obtenerSimple(@PathVariable Long id) {
        return ResponseEntity.ok(competicionService.obtenerPorIdSimple(id));
    }

    /**
     * GET /competiciones/{id}/info — obtiene la información general de una competición (sin detalle completo).
     *
     * @param id identificador único de la competición
     * @return ResponseEntity con el CompeticionInfoDTO de la competición solicitada
     */
    @GetMapping("/{id}/info")
    public ResponseEntity<CompeticionInfoDTO> obtenerInfo(@PathVariable Long id) {
        return ResponseEntity.ok(competicionService.obtenerPorIdInfo(id));
    }

    /**
     * GET /competiciones/{id}/detalle — obtiene la vista completa de una competición, incluyendo equipos y configuración.
     *
     * @param id identificador único de la competición
     * @return ResponseEntity con el CompeticionDetalleDTO de la competición solicitada
     */
    @GetMapping("/{id}/detalle")
    public ResponseEntity<CompeticionDetalleDTO> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(competicionService.obtenerPorIdDetalle(id));
    }

    // ==================== CONSULTAS POR USUARIO ====================

    /**
     * GET /competiciones/mis-competiciones/creador — obtiene todas las competiciones creadas por el usuario autenticado.
     *
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con la lista de CompeticionSimpleDTO creadas por el usuario
     */
    @GetMapping("/mis-competiciones/creador")
    public ResponseEntity<List<CompeticionSimpleDTO>> obtenerPorCreador(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(competicionService.obtenerPorCreador(principal.getId()));
    }

    /**
     * GET /competiciones/mis-competiciones/participante — obtiene todas las competiciones en las que participa el usuario autenticado.
     *
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con la lista de CompeticionSimpleDTO en las que el usuario participa
     */
    @GetMapping("/mis-competiciones/participante")
    public ResponseEntity<List<CompeticionSimpleDTO>> obtenerPorParticipante(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(competicionService.obtenerPorParticipante(principal.getId()));
    }

    /**
     * GET /competiciones/mis-competiciones/por-rol — devuelve las competiciones
     * del usuario autenticado agrupadas por rol (admin, manager, arbitro, jugador).
     * Permite que el dashboard muestre cuatro secciones separadas e incluye
     * las competiciones del usuario como jugador (no cubierto por
     * UsuarioRolCompeticion en el flujo de invitación actual).
     *
     * @param principal datos del usuario autenticado
     * @return DTO con las cuatro listas de competiciones
     */
    @GetMapping("/mis-competiciones/por-rol")
    public ResponseEntity<MisCompeticionesPorRolDTO> obtenerMisCompeticionesPorRol(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(competicionService.obtenerMisCompeticionesPorRol(principal.getId()));
    }

    // ==================== CRUD ====================

    /**
     * POST /competiciones — crea una nueva competición.
     * El usuario autenticado queda asignado automáticamente como administrador de la competición.
     *
     * @param request cuerpo con los datos de la nueva competición (nombre, deporte, formato, visibilidad, etc.)
     * @param userDetails datos del usuario autenticado que actúa como creador
     * @return ResponseEntity con el CompeticionDetalleDTO de la competición creada y estado 201 Created
     */
    @PostMapping
    public ResponseEntity<CompeticionDetalleDTO> crear(
            @Valid @RequestBody CompeticionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CompeticionDetalleDTO competicion = competicionService.crear(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(competicion);
    }

    /**
     * PUT /competiciones/{id} — actualiza los datos de una competición existente.
     * Reservado al admin de la competición (verificado vía RBAC). El identificador del
     * usuario que opera se toma del JWT, no del cliente.
     *
     * @param id identificador único de la competición a actualizar
     * @param request cuerpo con los nuevos datos de la competición
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con el CompeticionDetalleDTO actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<CompeticionDetalleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CompeticionUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(competicionService.actualizar(id, request, principal.getId()));
    }

    /**
     * DELETE /competiciones/{id} — elimina una competición y todos sus datos asociados.
     * Requiere rol de administrador de la competición (verificado via RBAC). El identificador
     * del usuario que opera se toma del JWT.
     *
     * @param id identificador único de la competición a eliminar
     * @param principal datos del usuario autenticado
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        competicionService.eliminar(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // ==================== ESTADO ====================

    /**
     * PATCH /competiciones/{id}/estado — cambia el estado de una competición (BORRADOR, ABIERTA, EN_CURSO, FINALIZADA).
     * Requiere rol de administrador de la competición (verificado via RBAC).
     *
     * @param id identificador único de la competición
     * @param estado nuevo estado a asignar a la competición
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con el CompeticionDetalleDTO con el estado actualizado
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<CompeticionDetalleDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Competicion.EstadoCompeticion estado,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(competicionService.cambiarEstado(id, estado, principal.getId()));
    }

    // ==================== TEMPORADA ====================

    /**
     * POST /competiciones/{id}/temporada — avanza a una nueva temporada dentro de la competición.
     * Requiere rol de administrador de la competición (verificado via RBAC).
     *
     * @param id identificador único de la competición
     * @param nuevaTemporada número de la nueva temporada a establecer
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con el CompeticionDetalleDTO con la temporada actualizada
     */
    @PostMapping("/{id}/temporada")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<CompeticionDetalleDTO> cambiarTemporada(
            @PathVariable Long id,
            @RequestParam Integer nuevaTemporada,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(competicionService.cambiarTemporada(id, nuevaTemporada,
                principal.getId()));
    }

    // ==================== GESTIÓN DE EQUIPOS ====================

    /**
     * GET /competiciones/{id}/equipos/simple — lista los equipos inscritos en una competición en formato resumido.
     *
     * @param id identificador único de la competición
     * @return ResponseEntity con la lista de EquipoSimpleDTO de los equipos inscritos
     */
    @GetMapping("/{id}/equipos/simple")
    public ResponseEntity<List<EquipoSimpleDTO>> listarEquiposSimple(@PathVariable Long id)
    {
        return ResponseEntity.ok(competicionService.obtenerInscritosSimple(id));
    }

    /**
     * GET /competiciones/{id}/equipos/detalle — lista los equipos inscritos en una competición en formato completo.
     * El campo {@code codigoInvitacion} de los equipos privados solo se devuelve a
     * usuarios con permisos de gestión sobre cada equipo (creador, manager o admin
     * de competición); el resto recibe {@code null} aunque el equipo sea privado.
     *
     * @param id identificador único de la competición
     * @param principal datos del usuario autenticado (puede ser {@code null} en flujos públicos)
     * @return ResponseEntity con la lista de EquipoDetalleDTO de los equipos inscritos
     */
    @GetMapping("/{id}/equipos/detalle")
    public ResponseEntity<List<EquipoDetalleDTO>> listarEquiposDetalle(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long usuarioId = principal != null ? principal.getId() : null;
        boolean esAdminSistema = principal != null && principal.isEsAdminSistema();
        return ResponseEntity.ok(competicionService.obtenerInscritosDetalle(id, usuarioId, esAdminSistema));
    }

    /**
     * POST /competiciones/{id}/equipos/{equipoId} — inscribe un equipo en una competición.
     * Requiere rol de administrador de la competición (verificado via RBAC).
     *
     * @param id identificador único de la competición
     * @param equipoId identificador del equipo a inscribir
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con mensaje de confirmación y estado 201 Created
     */
    @PostMapping("/{id}/equipos/{equipoId}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<Map<String, String>> inscribirEquipo(
            @PathVariable Long id,
            @PathVariable Long equipoId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        competicionService.altaEquipo(id, equipoId, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Equipo inscrito en la competición"));
    }

    /**
     * DELETE /competiciones/{id}/equipos/{equipoId} — retira un equipo de una competición.
     * Requiere rol de administrador de la competición (verificado via RBAC).
     *
     * @param id identificador único de la competición
     * @param equipoId identificador del equipo a retirar
     * @param principal datos del usuario autenticado
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}/equipos/{equipoId}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<Void> retirarEquipo(
            @PathVariable Long id,
            @PathVariable Long equipoId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        competicionService.bajaEquipo(id, equipoId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // ==================== JUGADORES (BÚSQUEDA CON SCOPE) ====================

    /**
     * GET /competiciones/{competicionId}/jugadores/buscar — busca jugadores cuyo
     * nombre o apellidos coincidan con la cadena indicada, restringiendo el censo
     * al de la competición. Sustituye al uso del endpoint global
     * {@code /jugadores/buscar} desde el frontend al añadir un jugador a un equipo,
     * para no exponer datos de jugadores ajenos a la competición.
     *
     * <p>Acceso reservado a miembros de la competición (admin, manager o jugador).</p>
     *
     * @param competicionId identificador de la competición
     * @param search cadena a buscar (puede ser vacía)
     * @param pageable paginación (por defecto 10 por página)
     * @return ResponseEntity con la página de JugadorSimpleDTO acotada a la competición
     */
    @GetMapping("/{competicionId}/jugadores/buscar")
    @PreAuthorize("@rbacService.isMiembroCompeticion(#competicionId, authentication)")
    public ResponseEntity<PageResponse<JugadorSimpleDTO>> buscarJugadores(
            @PathVariable Long competicionId,
            @RequestParam(required = false, defaultValue = "") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(jugadorService.buscarPorCompeticion(competicionId, search, pageable));
    }

    // ==================== GESTIÓN DE USUARIOS ====================

    /**
     * GET /competiciones/{id}/usuarios — lista todos los usuarios con sus roles en una competición.
     * Requiere rol de administrador de la competición (verificado via RBAC).
     *
     * @param id identificador único de la competición
     * @return ResponseEntity con la lista de UsuarioRolCompeticionDTO con los roles asignados
     */
    @GetMapping("/{id}/usuarios")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<List<UsuarioRolCompeticionDTO>> listarUsuarios(
            @PathVariable Long id) {
        return ResponseEntity.ok(competicionService.obtenerUsuariosConRol(id));
    }

    /**
     * DELETE /competiciones/{id}/usuarios/{usuarioId} — elimina el rol de un usuario en una competición.
     * Requiere rol de administrador de la competición (verificado via RBAC). El identificador
     * del solicitante se toma del JWT.
     *
     * @param id identificador único de la competición
     * @param usuarioId identificador del usuario al que se le retira el rol
     * @param principal datos del usuario autenticado (solicitante)
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}/usuarios/{usuarioId}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#id, authentication)")
    public ResponseEntity<Void> quitarUsuario(
            @PathVariable Long id,
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        competicionService.quitarUsuario(id, usuarioId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
