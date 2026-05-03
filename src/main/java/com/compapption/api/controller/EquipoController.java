package com.compapption.api.controller;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorDetalleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.request.equipo.EquipoCreateRequest;
import com.compapption.api.request.equipo.EquipoUpdateRequest;
import com.compapption.api.request.jugador.JugadorCreateRequest;
import com.compapption.api.request.page.PageResponse;
import com.compapption.api.service.EquipoService;
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
 * Controlador REST para la gestión de equipos. Expone endpoints bajo la ruta base /equipos.
 * Gestiona el CRUD de equipos, la administración de su plantilla de jugadores y la asignación de managers.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/equipos")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    /// === END POINTS CRUD === ///

    /**
     * GET /equipos — busca equipos por nombre con paginación. Por defecto solo
     * devuelve equipos públicos para alimentar el selector de inscripción en
     * competiciones; con {@code soloPublicos=false} incluye también los privados
     * (uso administrativo).
     *
     * @param search        término de búsqueda para filtrar equipos por nombre
     * @param soloPublicos  si {@code true} (por defecto) omite los privados
     * @param pageable      parámetros de paginación y ordenación
     * @return ResponseEntity con una página de EquipoSimpleDTO que coinciden con la búsqueda
     */
    @GetMapping
    public ResponseEntity<PageResponse<EquipoSimpleDTO>> buscar(
            @RequestParam String search,
            @RequestParam(defaultValue = "true") boolean soloPublicos,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(equipoService.buscar(search, soloPublicos, pageable));
    }

    /**
     * GET /equipos/codigo/{codigo} — localiza un equipo privado por su código
     * de invitación. Devuelve los datos básicos para que el admin que va a
     * invitar pueda confirmar la elección antes de mandar la invitación.
     *
     * @param codigo código compartido por el creador del equipo
     * @return ResponseEntity con el EquipoSimpleDTO del equipo
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<EquipoSimpleDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(equipoService.buscarPorCodigo(codigo));
    }

    /**
     * GET /equipos/mis-equipos/manager — obtiene todos los equipos en los que el usuario autenticado es manager.
     *
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con la lista de EquipoSimpleDTO donde el usuario gestiona como manager
     */
    @GetMapping("mis-equipos/manager")
    public ResponseEntity<List<EquipoSimpleDTO>> buscarMisEquiposManager(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(equipoService.obtenerPorManager(principal.getId()));
    }

    /**
     * GET /equipos/mis-equipos/jugador — obtiene todos los equipos en los que el usuario autenticado participa como jugador.
     *
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con la lista de EquipoSimpleDTO donde el usuario está inscrito como jugador
     */
    @GetMapping("mis-equipos/jugador")
    public ResponseEntity<List<EquipoSimpleDTO>> buscarMisEquiposJugador(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(equipoService.obtenerPorJugador(principal.getId()));
    }

    /**
     * GET /equipos/mis-equipos/creados — obtiene todos los equipos creados por el usuario autenticado,
     * independientemente de si están inscritos o no en una competición. Alimenta la
     * bandeja "Mis equipos" del dashboard tras crear un equipo nuevo.
     *
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con la lista de EquipoSimpleDTO creados por el usuario
     */
    @GetMapping("mis-equipos/creados")
    public ResponseEntity<List<EquipoSimpleDTO>> buscarMisEquiposCreados(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(equipoService.obtenerPorCreador(principal.getId()));
    }

    /**
     * GET /equipos/{id}/simple — obtiene la vista resumida de un equipo por su identificador.
     *
     * @param id identificador único del equipo
     * @return ResponseEntity con el EquipoSimpleDTO del equipo solicitado
     */
    @GetMapping("/{id}/simple")
    public ResponseEntity<EquipoSimpleDTO> buscarPorIdSimple(@PathVariable long id) {
        return ResponseEntity.ok(equipoService.obtenerPorIdSimple(id));
    }

    /**
     * GET /equipos/{id}/detalle — obtiene la vista completa de un equipo incluyendo su plantilla y manager.
     * El campo {@code codigoInvitacion} solo se devuelve a usuarios con permisos de
     * gestión sobre el equipo (creador, manager o admin de competición). El resto
     * recibe {@code null} aunque el equipo sea privado.
     *
     * @param id identificador único del equipo
     * @param principal datos del usuario autenticado (puede ser anónimo en /publico)
     * @return ResponseEntity con el EquipoDetalleDTO del equipo solicitado
     */
    @GetMapping("/{id}/detalle")
    public ResponseEntity<EquipoDetalleDTO> buscarPorIdDetalle(
            @PathVariable long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long usuarioId = principal != null ? principal.getId() : null;
        boolean esAdminSistema = principal != null && principal.isEsAdminSistema();
        return ResponseEntity.ok(equipoService.obtenerPorIdDetalle(id, usuarioId, esAdminSistema));
    }

    /**
     * POST /equipos — crea un nuevo equipo. Asigna automáticamente el usuario autenticado
     * como creador del equipo.
     *
     * @param request cuerpo con los datos del nuevo equipo (nombre, descripción, escudo)
     * @param userDetails principal autenticado; su {@code id} se usa como creador del equipo
     * @return ResponseEntity con el EquipoDetalleDTO del equipo creado y estado 201 Created
     */
    @PostMapping
    public ResponseEntity<EquipoDetalleDTO> crear(
            @Valid @RequestBody EquipoCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipoService.crear(request, userDetails.getId()));
    }

    /**
     * PUT /equipos/{id} — actualiza los datos de un equipo existente. Reservado al
     * creador, manager del equipo en alguna competición o admin de competición donde
     * participa (verificado vía RBAC).
     *
     * @param id identificador único del equipo a actualizar
     * @param request cuerpo con los nuevos datos del equipo
     * @return ResponseEntity con el EquipoSimpleDTO actualizado
     */
    @PutMapping("{id}")
    @PreAuthorize("@rbacService.puedeAdministrarEquipo(#id, authentication)")
    public ResponseEntity<EquipoSimpleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EquipoUpdateRequest request) {
        return ResponseEntity.ok(equipoService.actualizar(id,request));
    }

    /**
     * DELETE /equipos/{id} — elimina un equipo del sistema. Reservado al creador,
     * manager del equipo o admin de competición (verificado vía RBAC).
     *
     * @param id identificador único del equipo a eliminar
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@rbacService.puedeAdministrarEquipo(#id, authentication)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        equipoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /// === END POINTS GESTIÓN PLANTILLA === ///

    /**
     * GET /equipos/{id}/jugadores-detalle — lista los jugadores de un equipo en formato completo.
     *
     * @param id identificador único del equipo
     * @return ResponseEntity con la lista de JugadorDetalleDTO de la plantilla del equipo
     */
    @GetMapping("/{id}/jugadores-detalle")
    public ResponseEntity<List<JugadorDetalleDTO>> listarJugadoresDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.obtenerJugadoresDetalle(id));
    }

    /**
     * POST /equipos/{equipoId}/jugadores — crea un jugador "fantasma" (sin cuenta) y lo
     * inscribe en la plantilla del equipo en un solo paso. Requiere ser creador del
     * equipo, manager del equipo en alguna de sus competiciones o admin de alguna
     * competición donde el equipo participa.
     *
     * @param equipoId identificador único del equipo
     * @param request datos del jugador (nombre, apellidos, dorsal, posición, foto). El
     *                campo {@code usuarioId} se ignora — usa el flujo de vinculación.
     * @param dorsal dorsal opcional vía query (prevalece sobre el del request)
     * @return ResponseEntity con el JugadorDetalleDTO creado y estado 201 Created
     */
    @PostMapping("/{equipoId}/jugadores")
    @PreAuthorize("@rbacService.puedeCrearJugadorEnEquipo(#equipoId, authentication)")
    public ResponseEntity<JugadorDetalleDTO> crearJugadorFantasma(
            @PathVariable Long equipoId,
            @Valid @RequestBody JugadorCreateRequest request,
            @RequestParam(required = false) Integer dorsal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipoService.crearJugadorFantasma(equipoId, request, dorsal));
    }

    /**
     * POST /equipos/{id}/jugadores/{jugadorId} — inscribe un jugador existente en la
     * plantilla. Requiere ser creador del equipo, manager del equipo en alguna de sus
     * competiciones o admin de una competición donde el equipo participa.
     *
     * @param id identificador único del equipo
     * @param jugadorId identificador del jugador a inscribir
     * @param dorsal número de dorsal asignado al jugador (opcional)
     * @return ResponseEntity con mensaje de confirmación de inscripción
     */
    @PostMapping("/{id}/jugadores/{jugadorId}")
    @PreAuthorize("@rbacService.puedeGestionarPlantilla(#id, authentication)")
    public ResponseEntity<Map<String,String>> agregarJugador(
            @PathVariable Long id,
            @PathVariable Long jugadorId,
            @RequestParam(required = false) Integer dorsal) {
        equipoService.agregarJugador(id,jugadorId,dorsal);
        return ResponseEntity.ok(Map.of("message","Jugador inscrito en el equipo"));
    }

    /**
     * DELETE /equipos/{id}/jugadores/{jugadorId} — da de baja a un jugador de la plantilla.
     * Mismas reglas de autorización que la inscripción.
     *
     * @param id identificador único del equipo
     * @param jugadorId identificador del jugador a dar de baja
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("{id}/jugadores/{jugadorId}")
    @PreAuthorize("@rbacService.puedeGestionarPlantilla(#id, authentication)")
    public ResponseEntity<Void> quitarJugador(
            @PathVariable Long id,
            @PathVariable Long jugadorId) {
        equipoService.quitarJugador(id, jugadorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /equipos/{id}/jugadores/{jugadorId}/dorsal — actualiza el dorsal
     * de un jugador del equipo. Mismas reglas de autorización que la inscripción.
     * Si {@code dorsal} no se pasa, se limpia el valor.
     *
     * @param id        identificador del equipo
     * @param jugadorId identificador del jugador
     * @param dorsal    nuevo dorsal (opcional; si se omite se borra)
     * @return ResponseEntity con un mensaje de confirmación
     */
    @PatchMapping("/{id}/jugadores/{jugadorId}/dorsal")
    @PreAuthorize("@rbacService.puedeGestionarPlantilla(#id, authentication)")
    public ResponseEntity<Map<String, String>> actualizarDorsal(
            @PathVariable Long id,
            @PathVariable Long jugadorId,
            @RequestParam(required = false) Integer dorsal) {
        equipoService.actualizarDorsal(id, jugadorId, dorsal);
        return ResponseEntity.ok(Map.of("message", "Dorsal actualizado"));
    }

    /// === CÓDIGO DE INVITACIÓN === ///

    /**
     * POST /equipos/{id}/codigo-invitacion/regenerar — emite un código de
     * invitación nuevo para un equipo privado. El antiguo deja de ser válido.
     * Requiere los mismos permisos que la gestión de plantilla (creador,
     * manager o admin de comp donde participa).
     *
     * @param id identificador del equipo privado
     * @return EquipoDetalleDTO con el nuevo código generado
     */
    @PostMapping("/{id}/codigo-invitacion/regenerar")
    @PreAuthorize("@rbacService.puedeGestionarPlantilla(#id, authentication)")
    public ResponseEntity<EquipoDetalleDTO> regenerarCodigoInvitacion(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.regenerarCodigoInvitacion(id));
    }

    /// === AÑADIR MANAGER === ///

    /**
     * POST /equipos/{id}/managers — asigna un usuario como manager de un equipo en una competición concreta.
     * Reservado al admin de la competición (verificado vía RBAC).
     *
     * @param id identificador único del equipo
     * @param competicionId identificador de la competición en la que se asigna el rol de manager
     * @param usuarioId identificador del usuario que pasa a ser manager del equipo
     * @return ResponseEntity con mensaje de confirmación de la asignación
     */
    @PostMapping("/{id}/managers")
    @PreAuthorize("@rbacService.isAdminCompeticion(#competicionId, authentication)")
    public ResponseEntity<Map<String,String>> asignarManager(
            @PathVariable Long id,
            @RequestParam Long competicionId,
            @RequestParam Long usuarioId) {
        equipoService.asignarManager(id, competicionId, usuarioId);
        return ResponseEntity.ok(Map.of("message","Manager asignado correctamente"));
    }
}
