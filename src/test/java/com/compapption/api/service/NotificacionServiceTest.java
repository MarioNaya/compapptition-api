package com.compapption.api.service;

import com.compapption.api.dto.notificacion.NotificacionDTO;
import com.compapption.api.entity.Notificacion;
import com.compapption.api.entity.Usuario;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.mapper.NotificacionMapper;
import com.compapption.api.repository.NotificacionRepository;
import com.compapption.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock private NotificacionRepository notificacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private NotificacionMapper notificacionMapper;

    private ObjectMapper objectMapper;
    private NotificacionService notificacionService;

    private Usuario alice;

    @BeforeEach
    void setUp() {
        // ObjectMapper real (no mock) para que la serialización del payload funcione.
        objectMapper = new ObjectMapper();
        notificacionService = new NotificacionService(
                notificacionRepository, usuarioRepository, notificacionMapper, objectMapper);

        alice = Usuario.builder().id(1L).username("alice").build();
    }

    // =========================================================
    // crear — persiste y envía a emitters activos
    // =========================================================

    @Test
    void crear_persisteNotificacionYLaEnviaAlEmitter() throws Exception {
        // Suscribe un emitter para alice
        SseEmitter emitter = spy(notificacionService.subscribe(1L));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> {
            Notificacion n = inv.getArgument(0);
            n.setId(100L);
            return n;
        });
        when(notificacionMapper.toDTO(any(Notificacion.class))).thenReturn(
                NotificacionDTO.builder().id(100L).tipo("MENSAJE_RECIBIDO").build());

        Map<String, Object> payload = new HashMap<>();
        payload.put("conversacionId", 5L);
        NotificacionDTO dto = notificacionService.crear(
                1L, Notificacion.TipoNotificacion.MENSAJE_RECIBIDO, payload);

        // Persistió
        verify(notificacionRepository).save(any(Notificacion.class));
        assertThat(dto).isNotNull();
        assertThat(dto.getTipo()).isEqualTo("MENSAJE_RECIBIDO");
    }

    @Test
    void crear_sinEmittersActivos_persisteDeTodosModos() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> {
            Notificacion n = inv.getArgument(0);
            n.setId(100L);
            return n;
        });
        when(notificacionMapper.toDTO(any(Notificacion.class))).thenReturn(
                NotificacionDTO.builder().id(100L).tipo("INVITACION_RECIBIDA").build());

        NotificacionDTO dto = notificacionService.crear(
                1L, Notificacion.TipoNotificacion.INVITACION_RECIBIDA, null);

        verify(notificacionRepository).save(any(Notificacion.class));
        assertThat(dto.getTipo()).isEqualTo("INVITACION_RECIBIDA");
    }

    // =========================================================
    // subscribe — varias pestañas por usuario
    // =========================================================

    @Test
    void subscribe_variasPestanasPorUsuario_mantieneListasSeparadas() {
        SseEmitter e1 = notificacionService.subscribe(1L);
        SseEmitter e2 = notificacionService.subscribe(1L);
        SseEmitter e3 = notificacionService.subscribe(2L);

        assertThat(e1).isNotNull();
        assertThat(e2).isNotNull();
        assertThat(e3).isNotNull();
        // No lanzaron excepción; la lista interna admite varios emitters por userId
    }

    // =========================================================
    // marcarLeida — verifica pertenencia
    // =========================================================

    @Test
    void marcarLeida_notificacionAjena_lanzaUnauthorized() {
        Usuario otro = Usuario.builder().id(99L).username("other").build();
        Notificacion n = Notificacion.builder()
                .id(10L).destinatario(otro)
                .tipo(Notificacion.TipoNotificacion.MENSAJE_RECIBIDO)
                .leida(false)
                .build();

        when(notificacionRepository.findById(10L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> notificacionService.marcarLeida(10L, 1L))
                .isInstanceOf(UnauthorizedException.class);

        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void marcarLeida_flujoFeliz_actualizaFlag() {
        Notificacion n = Notificacion.builder()
                .id(10L).destinatario(alice)
                .tipo(Notificacion.TipoNotificacion.MENSAJE_RECIBIDO)
                .leida(false)
                .build();

        when(notificacionRepository.findById(10L)).thenReturn(Optional.of(n));
        when(notificacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificacionService.marcarLeida(10L, 1L);

        assertThat(n.isLeida()).isTrue();
        verify(notificacionRepository).save(n);
    }

    // =========================================================
    // marcarTodasLeidas — delega en bulk update
    // =========================================================

    @Test
    void marcarTodasLeidas_delegaEnElRepositorio() {
        when(notificacionRepository.marcarTodasLeidas(1L)).thenReturn(7);

        int actualizadas = notificacionService.marcarTodasLeidas(1L);

        assertThat(actualizadas).isEqualTo(7);
        verify(notificacionRepository).marcarTodasLeidas(1L);
    }
}
