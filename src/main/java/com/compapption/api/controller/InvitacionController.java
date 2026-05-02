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
     * GET /invitaciones/pendientes — obtiene las invitaciones pendientes de respuesta para el usuario autenticado.
     *
     * @param principal datos del usuario autenticado (destinatario)
     * @return ResponseEntity con la lista de InvitacionSimpleDTO pendientes del usuario
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<InvitacionSimpleDTO>> obtenerPendientes(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(invitacionService.obtenerPendientes(principal.getId()));
    }

    /**
     * GET /invitaciones/enviadas — obtiene las invitaciones enviadas por el usuario autenticado.
     *
     * @param principal datos del usuario autenticado (remitente)
     * @return ResponseEntity con la lista de InvitacionSimpleDTO enviadas por el usuario
     */
    @GetMapping("/enviadas")
    public ResponseEntity<List<InvitacionSimpleDTO>> obtenerEnviadas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(invitacionService.obtenerEnviadas(principal.getId()));
    }

    /**
     * GET /invitaciones/competicion/{competicionId} — obtiene todas las invitaciones asociadas a una competición.
     * Reservado al admin de la competición.
     *
     * @param competicionId identificador único de la competición
     * @return ResponseEntity con la lista de InvitacionSimpleDTO de la competición
     */
    @GetMapping("/competicion/{competicionId}")
    @PreAuthorize("@rbacService.isAdminCompeticion(#competicionId, authentication)")
    public ResponseEntity<List<InvitacionSimpleDTO>> obtenerPorCompeticion(
            @PathVariable Long competicionId) {
        return ResponseEntity.ok(invitacionService.obtenerPorCompeticion(competicionId));
    }

    /**
     * PUT /invitaciones/{token}/aceptar — acepta una invitación mediante su token único.
     * Asigna el rol correspondiente al usuario en la competición e invalida el token.
     * El identificador del usuario se toma del JWT, impidiendo aceptar en nombre de
     * un tercero (S-15 mitigado).
     *
     * @param token token UUID de la invitación a aceptar
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con el InvitacionDetalleDTO actualizado con estado ACEPTADA
     */
    @PutMapping("/{token}/aceptar")
    public ResponseEntity<InvitacionDetalleDTO> aceptar(
            @PathVariable String token,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(invitacionService.aceptarPorToken(token, principal.getId()));
    }

    /**
     * PUT /invitaciones/{token}/rechazar — rechaza una invitación mediante su token único.
     * Marca la invitación como rechazada e invalida el token. El identificador del usuario
     * se toma del JWT.
     *
     * @param token token UUID de la invitación a rechazar
     * @param principal datos del usuario autenticado
     * @return ResponseEntity con el InvitacionDetalleDTO actualizado con estado RECHAZADA
     */
    @PutMapping("/{token}/rechazar")
    public ResponseEntity<InvitacionDetalleDTO> rechazar(
            @PathVariable String token,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(invitacionService.rechazarPorToken(token, principal.getId()));
    }
}
