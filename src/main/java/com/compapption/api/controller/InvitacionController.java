package com.compapption.api.controller;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.dto.invitacionDTO.InvitacionDetalleDTO;
import com.compapption.api.dto.invitacionDTO.InvitacionSimpleDTO;
import com.compapption.api.request.invitacion.InvitacionCreateRequest;
import com.compapption.api.service.InvitacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * POST /invitaciones — crea una nueva invitación para que un usuario se una a una
     * competición o un equipo con el rol indicado. Genera un token UUID con validez de
     * 7 días, notifica al destinatario por email y, si ya está registrado, también vía SSE.
     * El emisor se toma del JWT, no del query string. La autorización se valida vía RBAC:
     * para invitar como ADMIN_COMPETICION o MANAGER_EQUIPO se exige ser admin de la
     * competición; para invitar como JUGADOR, poder gestionar la plantilla del equipo.
     *
     * @param request cuerpo con los datos de la invitación (destinatario por email o
     *                username, competición o equipo, rol)
     * @param userDetails principal autenticado; su {@code id} se usa como emisor
     * @return ResponseEntity con el InvitacionDetalleDTO creado y estado 201 Created
     */
    @PostMapping
    @PreAuthorize("@rbacService.puedeInvitar(#request, authentication)")
    public ResponseEntity<InvitacionDetalleDTO> crear(
            @Valid @RequestBody InvitacionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitacionService.crearInvitacion(userDetails.getId(), request));
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
