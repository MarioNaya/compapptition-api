package com.compapptition.api.service;

import com.compapptition.api.entity.EquipoJugador;
import com.compapptition.api.entity.Jugador;
import com.compapptition.api.entity.Usuario;
import com.compapptition.api.exception.BadRequestException;
import com.compapptition.api.exception.ResourceNotFoundException;
import com.compapptition.api.mapper.JugadorMapper;
import com.compapptition.api.repository.EquipoJugadorRepository;
import com.compapptition.api.repository.JugadorRepository;
import com.compapptition.api.repository.UsuarioRepository;
import com.compapptition.api.service.log.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JugadorServiceTest {

    @Mock private JugadorRepository jugadorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EquipoJugadorRepository equipoJugadorRepository;
    @Mock private JugadorMapper jugadorMapper;
    @Mock private LogService logService;

    @InjectMocks private JugadorService jugadorService;

    private Jugador jugador;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jugador = Jugador.builder().id(1L).nombre("Juan").apellidos("García").build();
        usuario = Usuario.builder().id(10L).username("juanuser").build();
    }

    // =========================================================
    // eliminar() — validaciones
    // =========================================================

    @Test
    void eliminar_jugadorNoExiste_lanzaResourceNotFound() {
        when(jugadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jugadorService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminar_jugadorEnEquipo_lanzaBadRequest() {
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(equipoJugadorRepository.existsByJugadorIdAndActivoTrue(1L)).thenReturn(true);

        assertThatThrownBy(() -> jugadorService.eliminar(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("equipo");
    }

    @Test
    void eliminar_jugadorSinEquipos_eliminaCorrectamente() {
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(equipoJugadorRepository.existsByJugadorIdAndActivoTrue(1L)).thenReturn(false);

        jugadorService.eliminar(1L);

        verify(jugadorRepository).delete(jugador);
    }

    // =========================================================
    // vincularUsuario() — validaciones
    // =========================================================

    @Test
    void vincularUsuario_jugadorNoExiste_lanzaResourceNotFound() {
        when(jugadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jugadorService.vincularUsuario(99L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void vincularUsuario_jugadorYaVinculado_lanzaBadRequest() {
        Jugador jugadorConUsuario = Jugador.builder().id(1L).nombre("Juan").usuario(usuario).build();

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugadorConUsuario));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> jugadorService.vincularUsuario(1L, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("vinculado");
    }

    @Test
    void vincularUsuario_usuarioYaTienePerfilJugador_lanzaBadRequest() {
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador)); // sin usuario
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(jugadorRepository.findByUsuarioId(10L)).thenReturn(Optional.of(jugador)); // ya tiene perfil

        assertThatThrownBy(() -> jugadorService.vincularUsuario(1L, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("perfil");
    }

    @Test
    void vincularUsuario_flujoFeliz_vinculaYGuarda() {
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(jugadorRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        when(jugadorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jugadorMapper.toUsuarioDTO(any())).thenReturn(null);

        jugadorService.vincularUsuario(1L, 10L);

        assertThat(jugador.getUsuario()).isEqualTo(usuario);
        verify(jugadorRepository).save(jugador);
    }
}
