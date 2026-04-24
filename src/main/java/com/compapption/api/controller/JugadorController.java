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

/**
 * Controlador REST para la gestión de jugadores. Expone endpoints bajo la ruta base /jugadores.
 * Gestiona el CRUD de jugadores y la vinculación de un jugador con su cuenta de usuario.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/jugadores")
@RequiredArgsConstructor
public class JugadorController {

    private final JugadorService jugadorService;

    /**
     * GET /jugadores/buscar — busca jugadores por nombre o criterio con paginación.
     *
     * @param search término de búsqueda para filtrar jugadores
     * @param pageable parámetros de paginación y ordenación (por defecto 20 por página)
     * @return ResponseEntity con una página de JugadorSimpleDTO que coinciden con la búsqueda
     */
    @GetMapping("/buscar")
    public ResponseEntity<PageResponse<JugadorSimpleDTO>> buscar(
            @RequestParam String search,
            @PageableDefault(size = 10)Pageable pageable){
        return ResponseEntity.ok(jugadorService.buscar(search, pageable));
    }

    /**
     * GET /jugadores/detalle/{id} — obtiene la vista completa de un jugador por su identificador.
     *
     * @param id identificador único del jugador
     * @return ResponseEntity con el JugadorDetalleDTO del jugador solicitado
     */
    @GetMapping("/detalle/{id}")
    public ResponseEntity<JugadorDetalleDTO> obtenerPorIdDetalle(@PathVariable Long id){
        return ResponseEntity.ok(jugadorService.obtenerPorIdDetalle(id));
    }

    /**
     * GET /jugadores/simple/{id} — obtiene la vista resumida de un jugador por su identificador.
     *
     * @param id identificador único del jugador
     * @return ResponseEntity con el JugadorSimpleDTO del jugador solicitado
     */
    @GetMapping("/simple/{id}")
    public ResponseEntity<JugadorSimpleDTO> obtenerPorIdSimple(@PathVariable Long id){
        return ResponseEntity.ok(jugadorService.obtenerPorIdSimple(id));
    }

    /**
     * POST /jugadores — crea un nuevo jugador en el sistema.
     *
     * @param request cuerpo con los datos del nuevo jugador (nombre, posición, datos físicos, etc.)
     * @return ResponseEntity con el JugadorDetalleDTO del jugador creado y estado 201 Created
     */
    @PostMapping
    public ResponseEntity<JugadorDetalleDTO> crear(
            @Valid @RequestBody JugadorCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(jugadorService.crear(request));
    }

    /**
     * PUT /jugadores/{id} — actualiza los datos de un jugador existente.
     *
     * @param id identificador único del jugador a actualizar
     * @param request cuerpo con los nuevos datos del jugador
     * @return ResponseEntity con el JugadorDetalleDTO actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<JugadorDetalleDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody JugadorUpdateRequest request){
        return ResponseEntity.ok(jugadorService.actualizar(id, request));
    }

    /**
     * DELETE /jugadores/{id} — elimina un jugador del sistema.
     *
     * @param id identificador único del jugador a eliminar
     * @return ResponseEntity vacío con estado 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        jugadorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /jugadores/{id}/vincular/{usuarioId} — vincula un jugador con una cuenta de usuario existente.
     * Permite que el usuario controle su propio perfil de jugador.
     *
     * @param id identificador único del jugador
     * @param usuarioId identificador del usuario a vincular con el jugador
     * @return ResponseEntity con el JugadorUsuarioDTO que refleja la vinculación realizada
     */
    @PostMapping("/{id}/vincular/{usuarioId}")
    public ResponseEntity<JugadorUsuarioDTO> vincularUsuario(
            @PathVariable Long id,
            @PathVariable Long usuarioId){
        return ResponseEntity.ok(jugadorService.vincularUsuario(id,usuarioId));
    }
}
