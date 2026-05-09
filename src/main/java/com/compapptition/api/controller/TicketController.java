package com.compapptition.api.controller;

import com.compapptition.api.config.CustomUserDetails;
import com.compapptition.api.dto.ticket.ActualizarEstadoTicketRequest;
import com.compapptition.api.dto.ticket.CrearTicketRequest;
import com.compapptition.api.dto.ticket.TicketDetalleDTO;
import com.compapptition.api.dto.ticket.TicketSimpleDTO;
import com.compapptition.api.entity.EstadoTicket;
import com.compapptition.api.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST de tickets de soporte. Expone endpoints bajo {@code /tickets}.
 *
 * <p>El sistema sustituye el canal mailto:* propuesto en la versión 0.0.1
 * inicial: cualquier usuario autenticado abre tickets desde el dropdown de
 * ayuda del navbar y el admin de sistema los gestiona desde el panel admin.</p>
 *
 * <p>Endpoints reservados al admin de sistema:
 * <ul>
 *   <li>{@code GET /tickets} — listado global con filtro opcional por estado.</li>
 *   <li>{@code GET /tickets/pendientes/count} — total de tickets activos.</li>
 *   <li>{@code PATCH /tickets/&#123;id&#125;/estado} — cambiar estado.</li>
 *   <li>{@code DELETE /tickets/&#123;id&#125;} — eliminar.</li>
 * </ul>
 * El resto son endpoints de usuario autenticado (cualquier rol).</p>
 *
 * @author Mario
 */
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * POST /tickets — crea un nuevo ticket de soporte a nombre del usuario
     * autenticado. Dispara email al admin de sistema.
     *
     * @param request     asunto y descripción del ticket
     * @param userDetails usuario autenticado
     * @return DTO de detalle del ticket creado
     */
    @PostMapping
    public ResponseEntity<TicketDetalleDTO> crear(
            @Valid @RequestBody CrearTicketRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ticketService.crear(userDetails.getId(), request));
    }

    /**
     * GET /tickets/mis — listado paginado de los tickets del usuario autenticado.
     */
    @GetMapping("/mis")
    public ResponseEntity<Page<TicketSimpleDTO>> listarMis(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ticketService.listarMisTickets(userDetails.getId(), pageable));
    }

    /**
     * GET /tickets — listado global paginado para el admin de sistema, con
     * filtro opcional por estado.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public ResponseEntity<Page<TicketSimpleDTO>> listarTodos(
            @RequestParam(required = false) EstadoTicket estado,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ticketService.listarTodos(estado, pageable));
    }

    /**
     * GET /tickets/pendientes/count — cuenta de tickets ABIERTO + EN_PROCESO.
     * Atajo del panel admin para mostrar un badge con el volumen pendiente.
     */
    @GetMapping("/pendientes/count")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public ResponseEntity<Map<String, Long>> contarPendientes() {
        return ResponseEntity.ok(Map.of("pendientes", ticketService.contarPendientes()));
    }

    /**
     * GET /tickets/&#123;id&#125; — detalle de un ticket. Accesible por su autor
     * y por cualquier admin de sistema.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketDetalleDTO> detalle(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean esAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN_SISTEMA"));
        return ResponseEntity.ok(ticketService.detalle(id, userDetails.getId(), esAdmin));
    }

    /**
     * PATCH /tickets/&#123;id&#125;/estado — cambia el estado de un ticket.
     * Reservado al admin de sistema. Emite notificación in-app al autor.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public ResponseEntity<TicketDetalleDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoTicketRequest request) {
        return ResponseEntity.ok(ticketService.actualizarEstado(id, request.getEstado()));
    }

    /**
     * DELETE /tickets/&#123;id&#125; — elimina un ticket. Reservado al admin de
     * sistema; el autor original no puede borrar sus tickets para preservar
     * el histórico.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_SISTEMA')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ticketService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
