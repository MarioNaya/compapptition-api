package com.compapption.api.controller;

import com.compapption.api.dto.invitacionDTO.InvitacionDetalleDTO;
import com.compapption.api.dto.invitacionDTO.InvitacionSimpleDTO;
import com.compapption.api.request.invitacion.InvitacionCreateRequest;
import com.compapption.api.service.InvitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de invitaciones. Expone endpoints bajo la ruta base /invitaciones.
 * Gestiona la creación de invitaciones a competiciones y la aceptación o rechazo
 * de las mismas mediante token UUID de un solo uso.
 *
 * @author Mario
 */
@RestController
@RequestMapping("/invitaciones")
@RequiredArgsConstructor
public class InvitacionController {

    private final InvitacionService invitacionService;

    /**
     * POST /invitaciones — crea una nueva invitación para que un usuario se una a una competición.
     * Genera un token UUID con validez de 7 días y lo asocia al destinatario.
     *
     * @param request cuerpo con los datos de la invitación (competición, destinatario, rol)
     * @param usuarioId identificador del usuario que envía la invitación
     * @return ResponseEntity con el InvitacionDetalleDTO de la invitación creada y estado 201 Created
     */
    @PostMapping
    public ResponseEntity<InvitacionDetalleDTO> crear(
            @Valid @RequestBody InvitacionCreateRequest request,
            @RequestParam Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitacionService.crearInvitacion(usuarioId, request));
    }

    /**
     * GET /invitaciones/pendientes — obtiene las invitaciones pendientes de respuesta para un usuario.
     *
     * @param usuarioId identificador del usuario destinatario
     * @return ResponseEntity con la lista de InvitacionSimpleDTO pendientes de ese usuario
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<InvitacionSimpleDTO>> obtenerPendientes(
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(invitacionService.obtenerPendientes(usuarioId));
    }

    /**
     * GET /invitaciones/enviadas — obtiene las invitaciones enviadas por un usuario.
     *
     * @param usuarioId identificador del usuario remitente
     * @return ResponseEntity con la lista de InvitacionSimpleDTO enviadas por ese usuario
     */
    @GetMapping("/enviadas")
    public ResponseEntity<List<InvitacionSimpleDTO>> obtenerEnviadas(
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(invitacionService.obtenerEnviadas(usuarioId));
    }

    /**
     * GET /invitaciones/competicion/{competicionId} — obtiene todas las invitaciones asociadas a una competición.
     *
     * @param competicionId identificador único de la competición
     * @return ResponseEntity con la lista de InvitacionSimpleDTO de la competición
     */
    @GetMapping("/competicion/{competicionId}")
    public ResponseEntity<List<InvitacionSimpleDTO>> obtenerPorCompeticion(
            @PathVariable Long competicionId) {
        return ResponseEntity.ok(invitacionService.obtenerPorCompeticion(competicionId));
    }

    /**
     * PUT /invitaciones/{token}/aceptar — acepta una invitación mediante su token único.
     * Asigna el rol correspondiente al usuario en la competición e invalida el token.
     *
     * @param token token UUID de la invitación a aceptar
     * @param usuarioId identificador del usuario que acepta la invitación
     * @return ResponseEntity con el InvitacionDetalleDTO actualizado con estado ACEPTADA
     */
    @PutMapping("/{token}/aceptar")
    public ResponseEntity<InvitacionDetalleDTO> aceptar(
            @PathVariable String token,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(invitacionService.aceptarPorToken(token, usuarioId));
    }

    /**
     * PUT /invitaciones/{token}/rechazar — rechaza una invitación mediante su token único.
     * Marca la invitación como rechazada e invalida el token.
     *
     * @param token token UUID de la invitación a rechazar
     * @param usuarioId identificador del usuario que rechaza la invitación
     * @return ResponseEntity con el InvitacionDetalleDTO actualizado con estado RECHAZADA
     */
    @PutMapping("/{token}/rechazar")
    public ResponseEntity<InvitacionDetalleDTO> rechazar(
            @PathVariable String token,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(invitacionService.rechazarPorToken(token, usuarioId));
    }
}
