package com.compapption.api.service;

import com.compapption.api.dto.ticket.CrearTicketRequest;
import com.compapption.api.dto.ticket.TicketDetalleDTO;
import com.compapption.api.dto.ticket.TicketSimpleDTO;
import com.compapption.api.entity.EstadoTicket;
import com.compapption.api.entity.Notificacion;
import com.compapption.api.entity.Ticket;
import com.compapption.api.entity.Usuario;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.mapper.TicketMapper;
import com.compapption.api.repository.TicketRepository;
import com.compapption.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Servicio de gestión de tickets de soporte.
 *
 * <p>Casos de uso:</p>
 * <ul>
 *   <li>Cualquier usuario autenticado abre un ticket desde el dropdown de ayuda
 *       del navbar; al crearse se envía un email al admin de sistema y el ticket
 *       queda en {@link EstadoTicket#ABIERTO}.</li>
 *   <li>El usuario consulta sus tickets y su estado actual desde la sección
 *       "Mis tickets".</li>
 *   <li>El admin de sistema lista todos los tickets desde el panel admin,
 *       filtra por estado, abre uno y cambia su estado. Cada cambio emite una
 *       notificación in-app al autor (vía {@link NotificacionService}).</li>
 * </ul>
 *
 * <p>El admin sistema puede ver y operar sobre cualquier ticket; los usuarios
 * sólo sobre los suyos.</p>
 *
 * @author Mario
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final TicketMapper ticketMapper;
    private final EmailService emailService;
    private final NotificacionService notificacionService;

    /**
     * Email del admin de sistema que recibe la notificación de nuevos tickets.
     * Por defecto, el remitente operativo del sistema (mismo buzón que envía
     * los emails automáticos).
     */
    @Value("${app.support.admin-email:no-reply@compapptition.com}")
    private String adminEmail;

    /// === CREACIÓN === ///

    /**
     * Crea un ticket en estado ABIERTO a nombre del usuario indicado.
     * Dispara de forma asíncrona el email de notificación al admin de
     * sistema con los detalles del ticket.
     *
     * @param usuarioId identificador del autor del ticket
     * @param request   datos del ticket (asunto y descripción)
     * @return DTO completo del ticket recién creado
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional
    public TicketDetalleDTO crear(Long usuarioId, CrearTicketRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        Ticket ticket = Ticket.builder()
                .usuario(usuario)
                .asunto(request.getAsunto())
                .descripcion(request.getDescripcion())
                .estado(EstadoTicket.ABIERTO)
                .build();

        ticket = ticketRepository.save(ticket);

        emailService.enviarNotificacionAdminTicket(
                adminEmail,
                ticket.getId(),
                ticket.getAsunto(),
                ticket.getDescripcion(),
                usuario.getUsername(),
                usuario.getEmail());

        log.info("Ticket #{} creado por usuario {} ({})",
                ticket.getId(), usuario.getUsername(), usuario.getId());

        return ticketMapper.toDetalleDTO(ticket);
    }

    /// === CONSULTAS === ///

    /**
     * Lista paginada de tickets del usuario indicado, ordenados por fecha
     * descendente. Vista "mis tickets" del autor.
     */
    @Transactional(readOnly = true)
    public Page<TicketSimpleDTO> listarMisTickets(Long usuarioId, Pageable pageable) {
        return ticketRepository
                .findByUsuarioIdOrderByFechaCreacionDesc(usuarioId, pageable)
                .map(ticketMapper::toSimpleDTO);
    }

    /**
     * Lista paginada global de tickets (panel admin), opcionalmente filtrada
     * por estado.
     *
     * @param estado   estado por el que filtrar; {@code null} para no filtrar
     * @param pageable configuración de paginación
     */
    @Transactional(readOnly = true)
    public Page<TicketSimpleDTO> listarTodos(EstadoTicket estado, Pageable pageable) {
        Page<Ticket> page = (estado == null)
                ? ticketRepository.findAllByOrderByFechaCreacionDesc(pageable)
                : ticketRepository.findByEstadoOrderByFechaCreacionDesc(estado, pageable);
        return page.map(ticketMapper::toSimpleDTO);
    }

    /**
     * Devuelve el detalle de un ticket. Sólo el autor o un admin de sistema
     * pueden consultarlo.
     *
     * @param ticketId      identificador del ticket
     * @param solicitanteId identificador del usuario que lo solicita
     * @param esAdmin       {@code true} si el solicitante es admin sistema
     * @return DTO de detalle del ticket
     * @throws ResourceNotFoundException si el ticket no existe
     * @throws UnauthorizedException     si el solicitante no es el autor ni admin
     */
    @Transactional(readOnly = true)
    public TicketDetalleDTO detalle(Long ticketId, Long solicitanteId, boolean esAdmin) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (!esAdmin && !ticket.getUsuario().getId().equals(solicitanteId)) {
            throw new UnauthorizedException("No puedes consultar tickets ajenos");
        }
        return ticketMapper.toDetalleDTO(ticket);
    }

    /**
     * Cuenta de tickets activos (ABIERTO + EN_PROCESO). Útil para el badge
     * del panel admin con tickets pendientes de atender.
     */
    @Transactional(readOnly = true)
    public long contarPendientes() {
        return ticketRepository.countByEstadoIn(
                List.of(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO));
    }

    /// === MUTACIONES ADMIN === ///

    /**
     * Actualiza el estado de un ticket. Reservado al admin de sistema.
     * Si el estado nuevo es distinto del anterior, emite una notificación
     * in-app al autor del ticket para que vea el cambio en su campana.
     *
     * @param ticketId   identificador del ticket
     * @param nuevoEstado nuevo estado a aplicar
     * @return DTO de detalle actualizado
     * @throws ResourceNotFoundException si el ticket no existe
     */
    @Transactional
    public TicketDetalleDTO actualizarEstado(Long ticketId, EstadoTicket nuevoEstado) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        EstadoTicket anterior = ticket.getEstado();
        if (anterior == nuevoEstado) {
            return ticketMapper.toDetalleDTO(ticket);
        }

        ticket.setEstado(nuevoEstado);
        ticket = ticketRepository.save(ticket);

        notificacionService.crear(
                ticket.getUsuario().getId(),
                Notificacion.TipoNotificacion.TICKET_ACTUALIZADO,
                Map.of(
                        "ticketId", ticket.getId(),
                        "asunto", ticket.getAsunto(),
                        "estadoAnterior", anterior.name(),
                        "estadoNuevo", nuevoEstado.name()));

        log.info("Ticket #{} cambiado de {} a {}", ticket.getId(), anterior, nuevoEstado);
        return ticketMapper.toDetalleDTO(ticket);
    }

    /**
     * Elimina un ticket. Reservado al admin de sistema; un usuario corriente
     * no puede borrar sus tickets para preservar el histórico de soporte.
     *
     * @param ticketId identificador del ticket
     * @throws ResourceNotFoundException si el ticket no existe
     */
    @Transactional
    public void eliminar(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        ticketRepository.delete(ticket);
        log.info("Ticket #{} eliminado por admin", ticketId);
    }
}
