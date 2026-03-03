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
     * GET /equipos — busca equipos por nombre o criterio de búsqueda con paginación.
     *
     * @param search término de búsqueda para filtrar equipos por nombre
     * @param pageable parámetros de paginación y ordenación (por defecto 20 por página)
     * @return ResponseEntity con una página de EquipoSimpleDTO que coinciden con la búsqueda
     */
    @GetMapping
    public ResponseEntity<PageResponse<EquipoSimpleDTO>> buscar(
            @RequestParam String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(equipoService.buscar(search, pageable));
    }

    /**
     * GET /equipos/mis-equipos/manager — obtiene todos los equipos en los que el usuario es manager.
     *
     * @param id identificador del usuario manager
     * @return ResponseEntity con la lista de EquipoSimpleDTO donde el usuario gestiona como manager
     */
    @GetMapping("mis-equipos/manager")
    public ResponseEntity<List<EquipoSimpleDTO>> buscarMisEquiposManager(@RequestParam long id) {
        return ResponseEntity.ok(equipoService.obtenerPorManager(id));
    }

    /**
     * GET /equipos/mis-equipos/jugador — obtiene todos los equipos en los que el usuario participa como jugador.
     *
     * @param id identificador del usuario jugador
     * @return ResponseEntity con la lista de EquipoSimpleDTO donde el usuario está inscrito como jugador
     */
    @GetMapping("mis-equipos/jugador")
    public ResponseEntity<List<EquipoSimpleDTO>> buscarMisEquiposJugador(@RequestParam long id) {
        return ResponseEntity.ok(equipoService.obtenerPorJugador(id));
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
     *
     * @param id identificador único del equipo
     * @return ResponseEntity con el EquipoDetalleDTO del equipo solicitado
     */
    @GetMapping("/{id}/detalle")
    public ResponseEntity<EquipoDetalleDTO> buscarPorIdDetalle(@PathVariable long id) {
        return ResponseEntity.ok(equipoService.obtenerPorIdDetalle(id));
    }

    /**
     * POST /equipos — crea un nuevo equipo.
     *
     * @param request cuerpo con los datos del nuevo equipo (nombre, deporte, tipo, escudo, etc.)
     * @return ResponseEntity con el EquipoDetalleDTO del equipo creado y estado 201 Created
     */
    @PostMapping
    public ResponseEntity<EquipoDetalleDTO> crear(@Valid @RequestBody EquipoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.crear(request));
    }

    /**
     * PUT /equipos/{id} — actualiza los datos de un equipo existente.
     *
     * @param id identificador único del equipo a actualizar
     * @param request cuerpo con los nuevos datos del equipo
     * @return ResponseEntity con el EquipoSimpleDTO actualizado
     */
    @PutMapping("{id}")
    public ResponseEntity<EquipoSimpleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EquipoUpdateRequest request) {
        return ResponseEntity.ok(equipoService.actualizar(id,request));
    }

    /**
     * DELETE /equipos/{id} — elimina un equipo del sistema.
     *
     * @param id identificador único del equipo a eliminar
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        equipoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /// === END POINTS GESTIÓN PLANTILLA === ///

    /**
     * GET /equipos/{id}/jugadores-simple — lista los jugadores de un equipo en formato resumido.
     *
     * @param id identificador único del equipo
     * @return ResponseEntity con la lista de JugadorSimpleDTO de la plantilla del equipo
     */
    @GetMapping("/{id}/jugadores-simple")
    public ResponseEntity<List<JugadorSimpleDTO>> listarJugadoresSimple(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.obtenerJugadoresSimple(id));
    }

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
     * POST /equipos/{id}/jugadores/{jugadorId} — inscribe un jugador en la plantilla de un equipo.
     *
     * @param id identificador único del equipo
     * @param jugadorId identificador del jugador a inscribir
     * @param dorsal número de dorsal asignado al jugador (opcional)
     * @return ResponseEntity con mensaje de confirmación de inscripción
     */
    @PostMapping("/{id}/jugadores/{jugadorId}")
    public ResponseEntity<Map<String,String>> agregarJugador(
            @PathVariable Long id,
            @PathVariable Long jugadorId,
            @RequestParam(required = false) Integer dorsal) {
        equipoService.agregarJugador(id,jugadorId,dorsal);
        return ResponseEntity.ok(Map.of("message","Jugador inscrito en el equipo"));
    }

    /**
     * DELETE /equipos/{id}/jugadores/{jugadorId} — elimina un jugador de la plantilla de un equipo.
     *
     * @param id identificador único del equipo
     * @param jugadorId identificador del jugador a dar de baja
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("{id}/jugadores/{jugadorId}")
    public ResponseEntity<Void> quitarJugador(
            @PathVariable Long id,
            @PathVariable Long jugadorId) {
        equipoService.quitarJugador(id, jugadorId);
        return ResponseEntity.noContent().build();
    }

    /// === AÑADIR MANAGER === ///

    /**
     * POST /equipos/{id}/managers — asigna un usuario como manager de un equipo en una competición concreta.
     *
     * @param id identificador único del equipo
     * @param competicionId identificador de la competición en la que se asigna el rol de manager
     * @param usuarioId identificador del usuario que pasa a ser manager del equipo
     * @return ResponseEntity con mensaje de confirmación de la asignación
     */
    @PostMapping("/{id}/managers")
    public ResponseEntity<Map<String,String>> asignarManager(
            @PathVariable Long id,
            @RequestParam Long competicionId,
            @RequestParam Long usuarioId) {
        equipoService.asignarManager(id, competicionId, usuarioId);
        return ResponseEntity.ok(Map.of("message","Manager asignado correctamente"));
    }
}