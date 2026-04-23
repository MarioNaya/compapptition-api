package com.compapption.api.service;

import com.compapption.api.dto.mensaje.ConversacionSimpleDTO;
import com.compapption.api.dto.mensaje.MensajeDTO;
import com.compapption.api.entity.Conversacion;
import com.compapption.api.entity.Mensaje;
import com.compapption.api.entity.Notificacion;
import com.compapption.api.entity.Usuario;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.mapper.MensajeMapper;
import com.compapption.api.repository.ConversacionRepository;
import com.compapption.api.repository.MensajeRepository;
import com.compapption.api.repository.UsuarioRepository;
import com.compapption.api.request.mensaje.ConversacionStartRequest;
import com.compapption.api.request.mensaje.MensajeCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensajeriaServiceTest {

    @Mock private ConversacionRepository conversacionRepository;
    @Mock private MensajeRepository mensajeRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private MensajeMapper mensajeMapper;
    @Mock private NotificacionService notificacionService;

    @InjectMocks private MensajeriaService mensajeriaService;

    private Usuario alice;
    private Usuario bob;

    @BeforeEach
    void setUp() {
        alice = Usuario.builder().id(1L).username("alice").build();
        bob = Usuario.builder().id(2L).username("bob").build();
    }

    // =========================================================
    // buscarOCrearConversacion — normaliza por id ascendente
    // =========================================================

    @Test
    void buscarOCrearConversacion_noExiste_creaConPareJaNormalizada() {
        // Bob (id=2) inicia conversación con Alice (id=1): usuarioA debe ser Alice (id menor)
        ConversacionStartRequest req = new ConversacionStartRequest(1L);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(conversacionRepository.findByUsuarioAAndUsuarioB(alice, bob)).thenReturn(Optional.empty());
        when(conversacionRepository.save(any(Conversacion.class))).thenAnswer(inv -> {
            Conversacion c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });
        when(mensajeRepository.findTopByConversacionId(anyLong(), any(Pageable.class))).thenReturn(List.of());
        when(mensajeRepository.countByConversacionIdAndAutorIdNotAndLeidoAtIsNull(anyLong(), anyLong())).thenReturn(0L);

        ConversacionSimpleDTO dto = mensajeriaService.buscarOCrearConversacion(2L, req);

        ArgumentCaptor<Conversacion> captor = ArgumentCaptor.forClass(Conversacion.class);
        verify(conversacionRepository).save(captor.capture());
        Conversacion creada = captor.getValue();
        assertThat(creada.getUsuarioA().getId()).isEqualTo(1L); // id menor
        assertThat(creada.getUsuarioB().getId()).isEqualTo(2L);

        // DTO devuelto desde la perspectiva del usuario 2 (bob) → el "otro" es alice
        assertThat(dto.getOtroUsuarioId()).isEqualTo(1L);
        assertThat(dto.getOtroUsuarioUsername()).isEqualTo("alice");
    }

    @Test
    void buscarOCrearConversacion_yaExiste_reutiliza() {
        // Alice (id=1) quiere hablar con Bob (id=2): usuarioA=Alice, usuarioB=Bob
        ConversacionStartRequest req = new ConversacionStartRequest(2L);
        Conversacion existente = Conversacion.builder()
                .id(50L).usuarioA(alice).usuarioB(bob).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(conversacionRepository.findByUsuarioAAndUsuarioB(alice, bob)).thenReturn(Optional.of(existente));
        when(mensajeRepository.findTopByConversacionId(anyLong(), any(Pageable.class))).thenReturn(List.of());
        when(mensajeRepository.countByConversacionIdAndAutorIdNotAndLeidoAtIsNull(anyLong(), anyLong())).thenReturn(0L);

        ConversacionSimpleDTO dto = mensajeriaService.buscarOCrearConversacion(1L, req);

        verify(conversacionRepository, never()).save(any());
        assertThat(dto.getId()).isEqualTo(50L);
    }

    // =========================================================
    // listarMensajes — valida pertenencia
    // =========================================================

    @Test
    void listarMensajes_usuarioNoEsParticipante_lanzaUnauthorized() {
        Conversacion c = Conversacion.builder().id(1L).usuarioA(alice).usuarioB(bob).build();
        when(conversacionRepository.findById(1L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> mensajeriaService.listarMensajes(1L, 99L, PageRequest.of(0, 10)))
                .isInstanceOf(UnauthorizedException.class);
    }

    // =========================================================
    // enviarMensaje — actualiza fechaUltimoMensaje y dispara notificación
    // =========================================================

    @Test
    void enviarMensaje_flujoFeliz_actualizaFechaYNotificaAlOtro() {
        Conversacion conv = Conversacion.builder().id(1L).usuarioA(alice).usuarioB(bob).build();
        MensajeCreateRequest req = new MensajeCreateRequest("Hola Bob!");

        when(conversacionRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> {
            Mensaje m = inv.getArgument(0);
            m.setId(123L);
            return m;
        });
        when(mensajeMapper.toDTO(any(Mensaje.class))).thenReturn(new MensajeDTO());

        mensajeriaService.enviarMensaje(1L, 1L /* alice */, req);

        // La fechaUltimoMensaje se actualiza y se guarda la conversación
        assertThat(conv.getFechaUltimoMensaje()).isNotNull();
        verify(conversacionRepository).save(conv);

        // Se dispara notificación al OTRO usuario (bob, id=2)
        verify(notificacionService).crear(
                eq(2L),
                eq(Notificacion.TipoNotificacion.MENSAJE_RECIBIDO),
                any(Map.class));
    }

    @Test
    void enviarMensaje_usuarioNoEsParticipante_lanzaUnauthorized() {
        Conversacion conv = Conversacion.builder().id(1L).usuarioA(alice).usuarioB(bob).build();
        when(conversacionRepository.findById(1L)).thenReturn(Optional.of(conv));

        assertThatThrownBy(() ->
                mensajeriaService.enviarMensaje(1L, 99L, new MensajeCreateRequest("hack")))
                .isInstanceOf(UnauthorizedException.class);

        verify(mensajeRepository, never()).save(any());
        verify(notificacionService, never()).crear(anyLong(), any(), any());
    }

    // =========================================================
    // marcarComoLeido — delega en el repositorio
    // =========================================================

    @Test
    void marcarComoLeido_flujoFeliz_llamaAlRepositorioConUsuarioYConversacion() {
        Conversacion conv = Conversacion.builder().id(1L).usuarioA(alice).usuarioB(bob).build();

        when(conversacionRepository.findById(1L)).thenReturn(Optional.of(conv));
        when(mensajeRepository.marcarComoLeidos(eq(1L), eq(1L), any())).thenReturn(3);

        int actualizados = mensajeriaService.marcarComoLeido(1L, 1L);

        assertThat(actualizados).isEqualTo(3);
        verify(mensajeRepository).marcarComoLeidos(eq(1L), eq(1L), any());
    }
}
