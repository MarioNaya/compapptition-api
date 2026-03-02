package com.compapption.api.service;

import com.compapption.api.dto.estadisticaDTO.EstadisticaAcumuladaDTO;
import com.compapption.api.entity.*;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.EstadisticaMapper;
import com.compapption.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadisticaServiceTest {

    @Mock private EstadisticaJugadorEventoRepository estadisticaRepository;
    @Mock private CompeticionRepository competicionRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private TipoEstadisticaRepository tipoEstadisticaRepository;
    @Mock private EstadisticaMapper estadisticaMapper;

    @InjectMocks private EstadisticaService estadisticaService;

    private Jugador jugador;
    private TipoEstadistica tipoGoles;

    @BeforeEach
    void setUp() {
        jugador = Jugador.builder().id(1L).nombre("Juan").apellidos("García").build();
        tipoGoles = TipoEstadistica.builder().id(10L).nombre("Goles").build();
    }

    // =========================================================
    // obtenerAcumuladoEnCompeticion() — validaciones
    // =========================================================

    @Test
    void obtenerAcumuladoEnCompeticion_competicionNoExiste_lanzaResourceNotFound() {
        when(competicionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> estadisticaService.obtenerAcumuladoEnCompeticion(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerAcumuladoEnCompeticion_jugadorNoExiste_lanzaResourceNotFound() {
        when(competicionRepository.existsById(1L)).thenReturn(true);
        when(jugadorRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> estadisticaService.obtenerAcumuladoEnCompeticion(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // =========================================================
    // obtenerAcumuladoEnCompeticion() — suma de BigDecimal
    // =========================================================

    @Test
    void obtenerAcumuladoEnCompeticion_tresGolesEnDosEventos_sumaTotal3() {
        EstadisticaJugadorEvento e1 = estadistica(jugador, tipoGoles, new BigDecimal("2"));
        EstadisticaJugadorEvento e2 = estadistica(jugador, tipoGoles, new BigDecimal("1"));

        when(competicionRepository.existsById(1L)).thenReturn(true);
        when(jugadorRepository.existsById(1L)).thenReturn(true);
        when(estadisticaRepository.findByCompeticionIdAndJugadorId(1L, 1L))
                .thenReturn(List.of(e1, e2));

        List<EstadisticaAcumuladaDTO> resultado = estadisticaService.obtenerAcumuladoEnCompeticion(1L, 1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTotal()).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(resultado.get(0).getTipoEstadisticaId()).isEqualTo(10L);
        assertThat(resultado.get(0).getJugadorId()).isEqualTo(1L);
    }

    @Test
    void obtenerAcumuladoEnCompeticion_dosTiposEstadistica_devuelveDosAcumulados() {
        TipoEstadistica tipoAsistencias = TipoEstadistica.builder().id(20L).nombre("Asistencias").build();

        EstadisticaJugadorEvento e1 = estadistica(jugador, tipoGoles, new BigDecimal("3"));
        EstadisticaJugadorEvento e2 = estadistica(jugador, tipoAsistencias, new BigDecimal("5"));

        when(competicionRepository.existsById(1L)).thenReturn(true);
        when(jugadorRepository.existsById(1L)).thenReturn(true);
        when(estadisticaRepository.findByCompeticionIdAndJugadorId(1L, 1L))
                .thenReturn(List.of(e1, e2));

        List<EstadisticaAcumuladaDTO> resultado = estadisticaService.obtenerAcumuladoEnCompeticion(1L, 1L);

        assertThat(resultado).hasSize(2);
        // Ordenados por tipoEstadisticaId: goles(10) antes que asistencias(20)
        assertThat(resultado.get(0).getTipoEstadisticaId()).isEqualTo(10L);
        assertThat(resultado.get(1).getTipoEstadisticaId()).isEqualTo(20L);
    }

    @Test
    void obtenerAcumuladoEnCompeticion_sinEstadisticas_devuelveListaVacia() {
        when(competicionRepository.existsById(1L)).thenReturn(true);
        when(jugadorRepository.existsById(1L)).thenReturn(true);
        when(estadisticaRepository.findByCompeticionIdAndJugadorId(1L, 1L))
                .thenReturn(List.of());

        List<EstadisticaAcumuladaDTO> resultado = estadisticaService.obtenerAcumuladoEnCompeticion(1L, 1L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void obtenerAcumuladoEnCompeticion_jugadorSinApellidos_nombreSoloConNombre() {
        Jugador jugadorSinApellidos = Jugador.builder().id(5L).nombre("Solo").apellidos(null).build();
        EstadisticaJugadorEvento e1 = estadistica(jugadorSinApellidos, tipoGoles, new BigDecimal("1"));

        when(competicionRepository.existsById(1L)).thenReturn(true);
        when(jugadorRepository.existsById(5L)).thenReturn(true);
        when(estadisticaRepository.findByCompeticionIdAndJugadorId(1L, 5L))
                .thenReturn(List.of(e1));

        List<EstadisticaAcumuladaDTO> resultado = estadisticaService.obtenerAcumuladoEnCompeticion(1L, 5L);

        assertThat(resultado.get(0).getJugadorNombre()).isEqualTo("Solo");
    }

    @Test
    void obtenerAcumuladoEnCompeticion_jugadorConApellidos_nombreCompleto() {
        EstadisticaJugadorEvento e1 = estadistica(jugador, tipoGoles, new BigDecimal("2"));

        when(competicionRepository.existsById(1L)).thenReturn(true);
        when(jugadorRepository.existsById(1L)).thenReturn(true);
        when(estadisticaRepository.findByCompeticionIdAndJugadorId(1L, 1L))
                .thenReturn(List.of(e1));

        List<EstadisticaAcumuladaDTO> resultado = estadisticaService.obtenerAcumuladoEnCompeticion(1L, 1L);

        assertThat(resultado.get(0).getJugadorNombre()).isEqualTo("Juan García");
    }

    // =========================================================
    // obtenerRankingEnCompeticion() — validaciones
    // =========================================================

    @Test
    void obtenerRankingEnCompeticion_competicionNoExiste_lanzaResourceNotFound() {
        when(competicionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> estadisticaService.obtenerRankingEnCompeticion(99L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerRankingEnCompeticion_tipoNoExiste_lanzaResourceNotFound() {
        when(competicionRepository.existsById(1L)).thenReturn(true);
        when(tipoEstadisticaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> estadisticaService.obtenerRankingEnCompeticion(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // =========================================================
    // obtenerRankingEnCompeticion() — ordenación por total desc
    // =========================================================

    @Test
    void obtenerRankingEnCompeticion_dosJugadores_ordenaPorTotalDesc() {
        Jugador jugadorB = Jugador.builder().id(2L).nombre("Pedro").apellidos("Pérez").build();

        EstadisticaJugadorEvento e1 = estadistica(jugador, tipoGoles, new BigDecimal("5")); // Juan: 5 goles
        EstadisticaJugadorEvento e2 = estadistica(jugadorB, tipoGoles, new BigDecimal("10")); // Pedro: 10 goles
        EstadisticaJugadorEvento e3 = estadistica(jugadorB, tipoGoles, new BigDecimal("3")); // Pedro acumulado: 13

        when(competicionRepository.existsById(1L)).thenReturn(true);
        when(tipoEstadisticaRepository.existsById(10L)).thenReturn(true);
        when(estadisticaRepository.findByCompeticionIdAndTipoEstadisticaId(1L, 10L))
                .thenReturn(List.of(e1, e2, e3));

        List<EstadisticaAcumuladaDTO> ranking = estadisticaService.obtenerRankingEnCompeticion(1L, 10L);

        assertThat(ranking).hasSize(2);
        // Pedro (13 goles) debe ir primero
        assertThat(ranking.get(0).getJugadorId()).isEqualTo(2L);
        assertThat(ranking.get(0).getTotal()).isEqualByComparingTo(new BigDecimal("13"));
        // Juan (5 goles) va segundo
        assertThat(ranking.get(1).getJugadorId()).isEqualTo(1L);
        assertThat(ranking.get(1).getTotal()).isEqualByComparingTo(new BigDecimal("5"));
    }

    // =========================================================
    // Helpers
    // =========================================================

    private EstadisticaJugadorEvento estadistica(Jugador jugador, TipoEstadistica tipo, BigDecimal valor) {
        return EstadisticaJugadorEvento.builder()
                .jugador(jugador)
                .tipoEstadistica(tipo)
                .valor(valor)
                .build();
    }
}
