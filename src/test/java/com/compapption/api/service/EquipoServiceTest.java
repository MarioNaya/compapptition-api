package com.compapption.api.service;

import com.compapption.api.entity.*;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.EquipoMapper;
import com.compapption.api.mapper.JugadorMapper;
import com.compapption.api.repository.*;
import com.compapption.api.service.log.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class EquipoServiceTest {

    @Mock private EquipoRepository equipoRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private EquipoJugadorRepository equipoJugadorRepository;
    @Mock private EquipoManagerRepository equipoManagerRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EquipoMapper equipoMapper;
    @Mock private JugadorMapper jugadorMapper;
    @Mock private LogService logService;

    @InjectMocks private EquipoService equipoService;

    private Equipo equipo;
    private Jugador jugador;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        equipo = Equipo.builder().id(1L).nombre("Test FC").build();
        jugador = Jugador.builder().id(10L).nombre("Juan").build();
        usuario = Usuario.builder().id(20L).username("manager").build();
    }

    // =========================================================
    // eliminar() — validaciones
    // =========================================================

    @Test
    void eliminar_equipoNoExiste_lanzaResourceNotFound() {
        when(equipoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipoService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminar_equipoInscritoEnCompeticion_lanzaBadRequest() {
        Set<CompeticionEquipo> inscripciones = new HashSet<>();
        inscripciones.add(CompeticionEquipo.builder().build());
        Equipo equipoInscrito = Equipo.builder().id(1L).nombre("Inscrito").competiciones(inscripciones).build();

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipoInscrito));

        assertThatThrownBy(() -> equipoService.eliminar(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("competicion");
    }

    @Test
    void eliminar_equipoSinCompeticiones_eliminaCorrectamente() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));

        equipoService.eliminar(1L);

        verify(equipoRepository).delete(equipo);
    }

    // =========================================================
    // agregarJugador() — validaciones
    // =========================================================

    @Test
    void agregarJugador_jugadorYaEnEquipo_lanzaBadRequest() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(jugadorRepository.findById(10L)).thenReturn(Optional.of(jugador));
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> equipoService.agregarJugador(1L, 10L, 7))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("pertenece");
    }

    @Test
    void agregarJugador_dorsalDuplicado_lanzaBadRequest() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(jugadorRepository.findById(10L)).thenReturn(Optional.of(jugador));
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(1L, 10L)).thenReturn(false);
        // dorsal 7 ya asignado
        when(equipoJugadorRepository.findByEquipoIdAndDorsalEquipo(1L, 7))
                .thenReturn(Optional.of(EquipoJugador.builder().build()));

        assertThatThrownBy(() -> equipoService.agregarJugador(1L, 10L, 7))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("dorsal");
    }

    @Test
    void agregarJugador_flujoFeliz_guardaEquipoJugador() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(jugadorRepository.findById(10L)).thenReturn(Optional.of(jugador));
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(1L, 10L)).thenReturn(false);
        when(equipoJugadorRepository.findByEquipoIdAndDorsalEquipo(1L, 9)).thenReturn(Optional.empty());

        equipoService.agregarJugador(1L, 10L, 9);

        ArgumentCaptor<EquipoJugador> captor = ArgumentCaptor.forClass(EquipoJugador.class);
        verify(equipoJugadorRepository).save(captor.capture());
        assertThat(captor.getValue().getJugador()).isEqualTo(jugador);
        assertThat(captor.getValue().getDorsalEquipo()).isEqualTo(9);
    }

    // =========================================================
    // quitarJugador() — validaciones
    // =========================================================

    @Test
    void quitarJugador_jugadorNoEnEquipo_lanzaResourceNotFound() {
        when(equipoJugadorRepository.findByEquipoIdAndJugadorId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipoService.quitarJugador(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void quitarJugador_flujoFeliz_marcaInactivoYGuarda() {
        EquipoJugador ej = EquipoJugador.builder().jugador(jugador).equipo(equipo).activo(true).build();
        when(equipoJugadorRepository.findByEquipoIdAndJugadorId(1L, 10L)).thenReturn(Optional.of(ej));
        when(equipoJugadorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        equipoService.quitarJugador(1L, 10L);

        assertThat(ej.isActivo()).isFalse();
        verify(equipoJugadorRepository).save(ej);
    }

    // =========================================================
    // asignarManager() — validaciones
    // =========================================================

    @Test
    void asignarManager_usuarioYaEsManager_lanzaBadRequest() {
        when(equipoRepository.existsById(1L)).thenReturn(true);
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(usuario));
        when(equipoManagerRepository.existsByEquipoIdAndCompeticionIdAndUsuarioId(1L, 5L, 20L)).thenReturn(true);

        assertThatThrownBy(() -> equipoService.asignarManager(1L, 5L, 20L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("manager");
    }
}
