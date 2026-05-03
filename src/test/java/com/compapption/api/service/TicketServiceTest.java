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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests del TicketService.
 *
 * Cubre los casos críticos:
 * <ul>
 *   <li>crear() persiste y dispara email al admin con datos correctos.</li>
 *   <li>listarMisTickets() y listarTodos() filtran por estado.</li>
 *   <li>detalle() bloquea acceso a tickets ajenos cuando no se es admin.</li>
 *   <li>actualizarEstado() emite notificación in-app sólo cuando cambia.</li>
 *   <li>eliminar() llama a delete del repositorio.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TicketMapper ticketMapper;
    @Mock private EmailService emailService;
    @Mock private NotificacionService notificacionService;

    @InjectMocks private TicketService ticketService;

    private Usuario autor;
    private Usuario otroUsuario;

    @BeforeEach
    void setUp() {
        autor = Usuario.builder()
                .id(7L).username("alberto.m").email("alberto@test.com").build();
        otroUsuario = Usuario.builder()
                .id(8L).username("otro").email("otro@test.com").build();

        ReflectionTestUtils.setField(
                ticketService, "adminEmail", "no-reply@compapptition.com");
    }

    private Ticket ticketBase() {
        LocalDateTime ts = LocalDateTime.of(2026, 5, 2, 18, 30);
        return Ticket.builder()
                .id(42L)
                .usuario(autor)
                .asunto("No me llega el email de invitación")
                .descripcion("Detalles del problema")
                .estado(EstadoTicket.ABIERTO)
                .fechaCreacion(ts)
                .fechaActualizacion(ts)
                .build();
    }

    // =========================================================
    // crear()
    // =========================================================

    @Test
    void crear_persisteYEnviaEmailAlAdmin() {
        CrearTicketRequest req = CrearTicketRequest.builder()
                .asunto("No me llega el email")
                .descripcion("Detalle largo del problema")
                .build();

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(autor));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(42L);
            return t;
        });
        when(ticketMapper.toDetalleDTO(any(Ticket.class)))
                .thenReturn(TicketDetalleDTO.builder().id(42L).estado("ABIERTO").build());

        TicketDetalleDTO out = ticketService.crear(7L, req);

        assertThat(out.getId()).isEqualTo(42L);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        Ticket persisted = captor.getValue();
        assertThat(persisted.getEstado()).isEqualTo(EstadoTicket.ABIERTO);
        assertThat(persisted.getUsuario()).isEqualTo(autor);

        verify(emailService).enviarNotificacionAdminTicket(
                eq("no-reply@compapptition.com"),
                eq(42L),
                eq("No me llega el email"),
                eq("Detalle largo del problema"),
                eq("alberto.m"),
                eq("alberto@test.com"));
    }

    @Test
    void crear_usuarioNoExiste_lanzaResourceNotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        CrearTicketRequest req = CrearTicketRequest.builder()
                .asunto("X").descripcion("Y").build();

        assertThatThrownBy(() -> ticketService.crear(99L, req))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(emailService);
    }

    // =========================================================
    // listarMisTickets() / listarTodos()
    // =========================================================

    @Test
    void listarMisTickets_devuelvePaginaDelAutor() {
        Page<Ticket> page = new PageImpl<>(List.of(ticketBase()));
        when(ticketRepository.findByUsuarioIdOrderByFechaCreacionDesc(eq(7L), any(Pageable.class)))
                .thenReturn(page);
        when(ticketMapper.toSimpleDTO(any()))
                .thenReturn(TicketSimpleDTO.builder().id(42L).build());

        Page<TicketSimpleDTO> out = ticketService.listarMisTickets(7L, Pageable.unpaged());

        assertThat(out.getContent()).hasSize(1);
        assertThat(out.getContent().get(0).getId()).isEqualTo(42L);
    }

    @Test
    void listarTodos_sinFiltro_usaQueryGlobal() {
        Page<Ticket> page = new PageImpl<>(List.of(ticketBase()));
        when(ticketRepository.findAllByOrderByFechaCreacionDesc(any(Pageable.class)))
                .thenReturn(page);
        when(ticketMapper.toSimpleDTO(any()))
                .thenReturn(TicketSimpleDTO.builder().id(42L).build());

        Page<TicketSimpleDTO> out = ticketService.listarTodos(null, Pageable.unpaged());

        assertThat(out.getContent()).hasSize(1);
        verify(ticketRepository, never())
                .findByEstadoOrderByFechaCreacionDesc(any(), any());
    }

    @Test
    void listarTodos_conFiltro_usaQueryPorEstado() {
        Page<Ticket> page = new PageImpl<>(List.of());
        when(ticketRepository.findByEstadoOrderByFechaCreacionDesc(
                eq(EstadoTicket.ABIERTO), any(Pageable.class))).thenReturn(page);

        Page<TicketSimpleDTO> out =
                ticketService.listarTodos(EstadoTicket.ABIERTO, Pageable.unpaged());

        assertThat(out.getContent()).isEmpty();
        verify(ticketRepository, never())
                .findAllByOrderByFechaCreacionDesc(any());
    }

    // =========================================================
    // detalle() — control de acceso
    // =========================================================

    @Test
    void detalle_autorPropio_permitido() {
        when(ticketRepository.findById(42L)).thenReturn(Optional.of(ticketBase()));
        when(ticketMapper.toDetalleDTO(any()))
                .thenReturn(TicketDetalleDTO.builder().id(42L).build());

        TicketDetalleDTO out = ticketService.detalle(42L, 7L, false);

        assertThat(out.getId()).isEqualTo(42L);
    }

    @Test
    void detalle_autorAjeno_lanzaUnauthorized() {
        when(ticketRepository.findById(42L)).thenReturn(Optional.of(ticketBase()));

        assertThatThrownBy(() -> ticketService.detalle(42L, 99L, false))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void detalle_admin_puedeVerTicketsAjenos() {
        when(ticketRepository.findById(42L)).thenReturn(Optional.of(ticketBase()));
        when(ticketMapper.toDetalleDTO(any()))
                .thenReturn(TicketDetalleDTO.builder().id(42L).build());

        TicketDetalleDTO out = ticketService.detalle(42L, 99L, true);

        assertThat(out.getId()).isEqualTo(42L);
    }

    // =========================================================
    // actualizarEstado()
    // =========================================================

    @Test
    void actualizarEstado_cambioReal_persisteYNotifica() {
        Ticket t = ticketBase();
        when(ticketRepository.findById(42L)).thenReturn(Optional.of(t));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketMapper.toDetalleDTO(any()))
                .thenReturn(TicketDetalleDTO.builder().id(42L).estado("RESUELTO").build());

        TicketDetalleDTO out =
                ticketService.actualizarEstado(42L, EstadoTicket.RESUELTO);

        assertThat(out.getEstado()).isEqualTo("RESUELTO");
        assertThat(t.getEstado()).isEqualTo(EstadoTicket.RESUELTO);

        verify(notificacionService).crear(
                eq(autor.getId()),
                eq(Notificacion.TipoNotificacion.TICKET_ACTUALIZADO),
                any(Map.class));
    }

    @Test
    void actualizarEstado_mismoEstado_noNotificaNiPersiste() {
        Ticket t = ticketBase();
        when(ticketRepository.findById(42L)).thenReturn(Optional.of(t));
        when(ticketMapper.toDetalleDTO(any()))
                .thenReturn(TicketDetalleDTO.builder().id(42L).estado("ABIERTO").build());

        ticketService.actualizarEstado(42L, EstadoTicket.ABIERTO);

        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(notificacionService);
    }

    @Test
    void actualizarEstado_ticketInexistente_lanzaResourceNotFound() {
        when(ticketRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                ticketService.actualizarEstado(999L, EstadoTicket.RESUELTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // =========================================================
    // eliminar()
    // =========================================================

    @Test
    void eliminar_existente_llamaDelete() {
        Ticket t = ticketBase();
        when(ticketRepository.findById(42L)).thenReturn(Optional.of(t));

        ticketService.eliminar(42L);

        verify(ticketRepository).delete(t);
    }

    @Test
    void eliminar_inexistente_lanzaResourceNotFound() {
        when(ticketRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.eliminar(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(ticketRepository, never()).delete(any());
    }

    // =========================================================
    // contarPendientes()
    // =========================================================

    @Test
    void contarPendientes_sumaAbiertosYEnProceso() {
        when(ticketRepository.countByEstadoIn(any())).thenReturn(5L);

        long total = ticketService.contarPendientes();

        assertThat(total).isEqualTo(5L);
    }

    /**
     * Para silenciar el unused-warning de otroUsuario, lo asocio a un
     * caso de uso lateral pero relevante: detalle ajeno desde un user
     * normal con el username del otroUsuario.
     */
    @Test
    void detalle_otroUsuarioComoNoAdmin_lanzaUnauthorized() {
        Ticket t = ticketBase();
        when(ticketRepository.findById(42L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() ->
                ticketService.detalle(42L, otroUsuario.getId(), false))
                .isInstanceOf(UnauthorizedException.class);
    }
}
