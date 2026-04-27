package com.compapption.api.controller;

import com.compapption.api.config.CustomUserDetails;
import com.compapption.api.dto.jugadorDTO.SolicitudVinculacionJugadorDTO;
import com.compapption.api.request.jugador.SolicitudVinculacionAdminRequest;
import com.compapption.api.request.jugador.SolicitudVinculacionAutoRequest;
import com.compapption.api.service.SolicitudVinculacionJugadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para las solicitudes de vinculación entre un jugador (sin cuenta)
 * y un usuario registrado, sujetas a doble validación.
 *
 * <p>Existen dos rutas para iniciar la solicitud según quién la firma:
 * <ul>
 *   <li>{@code POST /jugadores/{jugadorId}/solicitudes-vinculacion} — la inicia un
 *       admin/manager del equipo del jugador, queda pendiente de la aceptación del
 *       usuario candidato.</li>
 *   <li>{@code POST /jugadores/{jugadorId}/solicitudes-vinculacion/auto} — la inicia
 *       el propio usuario candidato ("este jugador soy yo"), queda pendiente de la
 *       aprobación del lado admin/manager.</li>
 * </ul>
 * La aceptación y el rechazo viven bajo la ruta {@code /solicitudes-vinculacion}
 * porque la legitimidad del aprobador no depende del jugador, sino del estado y
 * del equipo asociado a la solicitud.</p>
 *
 * @author Mario
 */
@RestController
@RequiredArgsConstructor
public class SolicitudVinculacionJugadorController {

    private final SolicitudVinculacionJugadorService service;

    /**
     * Inicia una solicitud desde el lado admin/manager. Requiere poder gestionar la
     * plantilla del equipo indicado en el body.
     */
    @PostMapping("/jugadores/{jugadorId}/solicitudes-vinculacion")
    @PreAuthorize("@rbacService.puedeGestionarPlantilla(#request.equipoId, authentication)")
    public ResponseEntity<SolicitudVinculacionJugadorDTO> iniciarComoAdmin(
            @PathVariable Long jugadorId,
            @Valid @RequestBody SolicitudVinculacionAdminRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.iniciarComoAdmin(jugadorId, request, userDetails.getId()));
    }

    /**
     * El propio usuario reclama el perfil del jugador. Cualquier autenticado puede
     * hacerlo siempre que el jugador esté en el equipo indicado y aún no tenga cuenta.
     */
    @PostMapping("/jugadores/{jugadorId}/solicitudes-vinculacion/auto")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudVinculacionJugadorDTO> iniciarComoUsuario(
            @PathVariable Long jugadorId,
            @Valid @RequestBody SolicitudVinculacionAutoRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.iniciarComoUsuario(jugadorId, request, userDetails.getId()));
    }

    /**
     * Acepta la solicitud. La autorización del firmante depende del estado actual
     * de la solicitud (la valida el service): si está pendiente del usuario, debe
     * firmarla el usuario candidato; si está pendiente del admin, alguien con
     * legitimidad sobre la plantilla del equipo.
     */
    @PostMapping("/solicitudes-vinculacion/{id}/aceptar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudVinculacionJugadorDTO> aceptar(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.aceptar(id, auth));
    }

    @PostMapping("/solicitudes-vinculacion/{id}/rechazar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudVinculacionJugadorDTO> rechazar(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.rechazar(id, auth));
    }

    /**
     * Bandeja de pendientes para el usuario logado: incluye tanto las que
     * requieren su aprobación como usuario candidato como las que requieren
     * su aprobación como admin/manager.
     */
    @GetMapping("/solicitudes-vinculacion/pendientes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SolicitudVinculacionJugadorDTO>> pendientes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(service.pendientesParaUsuario(userDetails.getId()));
    }
}
