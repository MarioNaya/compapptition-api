package com.compapption.api.service;

import com.compapption.api.entity.*;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.ClasificacionMapper;
import com.compapption.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClasificacionServiceTest {

    @Mock private ClasificacionRepository clasificacionRepository;
    @Mock private CompeticionRepository competicionRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private EventoEquipoRepository eventoEquipoRepository;
    @Mock private ClasificacionMapper clasificacionMapper;

    @InjectMocks private ClasificacionService clasificacionService;

    private Equipo equipoA;
    private Equipo equipoB;
    private ConfiguracionCompeticion config;
    private Competicion competicion;

    @BeforeEach
    void setUp() {
        equipoA = Equipo.builder().id(1L).nombre("Equipo A").build();
        equipoB = Equipo.builder().id(2L).nombre("Equipo B").build();

        config = ConfiguracionCompeticion.builder()
                .puntosVictoria(3)
                .puntosEmpate(1)
                .puntosDerrota(0)
                .build();

        competicion = Competicion.builder()
                .id(1L)
                .nombre("Liga Test")
                .temporadaActual(1)
                .configuracion(config)
                .build();
    }

    // =========================================================
    // calcularClasificacion() — resultados de partido
    // =========================================================

    @Test
    void calcularClasificacion_victoriaLocal_local3PuntosVisitante0() {
        Clasificacion clasA = clasificacionParaEquipo(equipoA);
        Clasificacion clasB = clasificacionParaEquipo(equipoB);

        Evento evento = eventoFinalizado(1L, 2, 1); // A gana 2-1
        EventoEquipo eeA = eventoEquipo(evento, equipoA, true);
        EventoEquipo eeB = eventoEquipo(evento, equipoB, false);

        prepararMocks(List.of(clasA, clasB), List.of(evento));
        when(eventoEquipoRepository.findByEventoId(1L)).thenReturn(List.of(eeA, eeB));

        clasificacionService.calcularClasificacion(1L);

        assertThat(clasA.getPuntos()).isEqualTo(3);
        assertThat(clasA.getVictorias()).isEqualTo(1);
        assertThat(clasA.getDerrotas()).isEqualTo(0);
        assertThat(clasA.getGolesFavor()).isEqualTo(2);
        assertThat(clasA.getGolesContra()).isEqualTo(1);

        assertThat(clasB.getPuntos()).isEqualTo(0);
        assertThat(clasB.getVictorias()).isEqualTo(0);
        assertThat(clasB.getDerrotas()).isEqualTo(1);
        assertThat(clasB.getGolesFavor()).isEqualTo(1);
        assertThat(clasB.getGolesContra()).isEqualTo(2);
    }

    @Test
    void calcularClasificacion_victoriaVisitante_visitante3PuntosLocal0() {
        Clasificacion clasA = clasificacionParaEquipo(equipoA);
        Clasificacion clasB = clasificacionParaEquipo(equipoB);

        Evento evento = eventoFinalizado(1L, 0, 3); // B gana 3-0
        EventoEquipo eeA = eventoEquipo(evento, equipoA, true);
        EventoEquipo eeB = eventoEquipo(evento, equipoB, false);

        prepararMocks(List.of(clasA, clasB), List.of(evento));
        when(eventoEquipoRepository.findByEventoId(1L)).thenReturn(List.of(eeA, eeB));

        clasificacionService.calcularClasificacion(1L);

        assertThat(clasA.getPuntos()).isEqualTo(0);
        assertThat(clasA.getDerrotas()).isEqualTo(1);
        assertThat(clasB.getPuntos()).isEqualTo(3);
        assertThat(clasB.getVictorias()).isEqualTo(1);
    }

    @Test
    void calcularClasificacion_empate_ambosObtienen1Punto() {
        Clasificacion clasA = clasificacionParaEquipo(equipoA);
        Clasificacion clasB = clasificacionParaEquipo(equipoB);

        Evento evento = eventoFinalizado(1L, 1, 1);
        EventoEquipo eeA = eventoEquipo(evento, equipoA, true);
        EventoEquipo eeB = eventoEquipo(evento, equipoB, false);

        prepararMocks(List.of(clasA, clasB), List.of(evento));
        when(eventoEquipoRepository.findByEventoId(1L)).thenReturn(List.of(eeA, eeB));

        clasificacionService.calcularClasificacion(1L);

        assertThat(clasA.getPuntos()).isEqualTo(1);
        assertThat(clasA.getEmpates()).isEqualTo(1);
        assertThat(clasB.getPuntos()).isEqualTo(1);
        assertThat(clasB.getEmpates()).isEqualTo(1);
    }

    // =========================================================
    // calcularClasificacion() — ordenación
    // =========================================================

    @Test
    void calcularClasificacion_tresequipos_ordenaPorPuntosDesc() {
        Equipo equipoC = Equipo.builder().id(3L).nombre("Equipo C").build();
        Clasificacion clasA = clasificacionParaEquipo(equipoA); // ganará 3pts
        Clasificacion clasB = clasificacionParaEquipo(equipoB); // empate 1pt
        Clasificacion clasC = clasificacionParaEquipo(equipoC); // derrota 0pts

        // A gana a C (3pts A, 0pts C)
        Evento e1 = eventoFinalizado(1L, 2, 0);
        EventoEquipo e1A = eventoEquipo(e1, equipoA, true);
        EventoEquipo e1C = eventoEquipo(e1, equipoC, false);

        // B empata con C (1pt B, 1pt C)
        Evento e2 = eventoFinalizado(2L, 1, 1);
        EventoEquipo e2B = eventoEquipo(e2, equipoB, true);
        EventoEquipo e2C2 = eventoEquipo(e2, equipoC, false);

        prepararMocks(List.of(clasA, clasB, clasC), List.of(e1, e2));
        when(eventoEquipoRepository.findByEventoId(1L)).thenReturn(List.of(e1A, e1C));
        when(eventoEquipoRepository.findByEventoId(2L)).thenReturn(List.of(e2B, e2C2));

        clasificacionService.calcularClasificacion(1L);

        // A→3pts, B→1pt, C→1pt (pero C tiene -2 de dif. goles)
        assertThat(clasA.getPosicion()).isEqualTo(1);
        assertThat(clasA.getPuntos()).isEqualTo(3);
    }

    // =========================================================
    // calcularClasificacion() — diferencia de goles y posición
    // =========================================================

    @Test
    void calcularClasificacion_calculaDiferenciaGolesCorrectamente() {
        Clasificacion clasA = clasificacionParaEquipo(equipoA);
        Clasificacion clasB = clasificacionParaEquipo(equipoB);

        Evento evento = eventoFinalizado(1L, 3, 1); // A gana 3-1
        prepararMocks(List.of(clasA, clasB), List.of(evento));
        when(eventoEquipoRepository.findByEventoId(1L))
                .thenReturn(List.of(eventoEquipo(evento, equipoA, true), eventoEquipo(evento, equipoB, false)));

        clasificacionService.calcularClasificacion(1L);

        assertThat(clasA.getDiferenciaGoles()).isEqualTo(2);   // 3-1
        assertThat(clasB.getDiferenciaGoles()).isEqualTo(-2);  // 1-3
    }

    @Test
    void calcularClasificacion_asignaPosicion1AlPrimero() {
        Clasificacion clasA = clasificacionParaEquipo(equipoA);
        Clasificacion clasB = clasificacionParaEquipo(equipoB);

        Evento evento = eventoFinalizado(1L, 1, 0); // A gana
        prepararMocks(List.of(clasA, clasB), List.of(evento));
        when(eventoEquipoRepository.findByEventoId(1L))
                .thenReturn(List.of(eventoEquipo(evento, equipoA, true), eventoEquipo(evento, equipoB, false)));

        clasificacionService.calcularClasificacion(1L);

        assertThat(clasA.getPosicion()).isEqualTo(1);
        assertThat(clasB.getPosicion()).isEqualTo(2);
    }

    // =========================================================
    // calcularClasificacion() — sin eventos
    // =========================================================

    @Test
    void calcularClasificacion_sinEventosFinalizados_clasificacionQuedaA0() {
        Clasificacion clasA = clasificacionParaEquipo(equipoA);
        Clasificacion clasB = clasificacionParaEquipo(equipoB);

        prepararMocks(List.of(clasA, clasB), List.of()); // sin eventos

        clasificacionService.calcularClasificacion(1L);

        assertThat(clasA.getPuntos()).isEqualTo(0);
        assertThat(clasB.getPuntos()).isEqualTo(0);
    }

    // =========================================================
    // calcularClasificacion() — competición no existe
    // =========================================================

    @Test
    void calcularClasificacion_competicionNoExiste_lanzaResourceNotFound() {
        when(competicionRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clasificacionService.calcularClasificacion(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // =========================================================
    // inicializarClasificacionEquipo()
    // =========================================================

    @Test
    void inicializarClasificacionEquipo_noExistePrevio_guardaClasificacion() {
        when(clasificacionRepository.findByCompeticionIdAndEquipoIdAndTemporada(1L, 1L, 1))
                .thenReturn(Optional.empty());

        clasificacionService.inicializarClasificacionEquipo(competicion, equipoA);

        ArgumentCaptor<Clasificacion> captor = ArgumentCaptor.forClass(Clasificacion.class);
        verify(clasificacionRepository).save(captor.capture());

        Clasificacion guardada = captor.getValue();
        assertThat(guardada.getPuntos()).isEqualTo(0);
        assertThat(guardada.getVictorias()).isEqualTo(0);
        assertThat(guardada.getPartidosJugados()).isEqualTo(0);
        assertThat(guardada.getTemporada()).isEqualTo(1);
        assertThat(guardada.getEquipo()).isEqualTo(equipoA);
        assertThat(guardada.getCompeticion()).isEqualTo(competicion);
    }

    @Test
    void inicializarClasificacionEquipo_yaExiste_noGuardaDuplicado() {
        Clasificacion existente = clasificacionParaEquipo(equipoA);
        when(clasificacionRepository.findByCompeticionIdAndEquipoIdAndTemporada(1L, 1L, 1))
                .thenReturn(Optional.of(existente));

        clasificacionService.inicializarClasificacionEquipo(competicion, equipoA);

        verify(clasificacionRepository, never()).save(any());
    }

    // =========================================================
    // resetearClasificacion()
    // =========================================================

    @Test
    void resetearClasificacion_poneATodosCero() {
        Clasificacion clas = Clasificacion.builder()
                .equipo(equipoA).competicion(competicion).temporada(1)
                .puntos(10).victorias(3).empates(1).derrotas(0)
                .golesFavor(8).golesContra(2).diferenciaGoles(6).partidosJugados(4)
                .build();

        when(clasificacionRepository.findByCompeticionIdAndTemporada(1L, 1))
                .thenReturn(List.of(clas));
        when(clasificacionRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        clasificacionService.resetearClasificacion(1L, 1);

        assertThat(clas.getPuntos()).isEqualTo(0);
        assertThat(clas.getVictorias()).isEqualTo(0);
        assertThat(clas.getGolesFavor()).isEqualTo(0);
        assertThat(clas.getDiferenciaGoles()).isEqualTo(0);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private void prepararMocks(List<Clasificacion> clasificaciones, List<Evento> eventos) {
        when(competicionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(competicion));
        when(clasificacionRepository.findByCompeticionIdAndTemporada(1L, 1))
                .thenReturn(clasificaciones);
        when(clasificacionRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepository.findFinalizadosByCompeticionIdAndTemporada(1L, 1))
                .thenReturn(eventos);
    }

    private Clasificacion clasificacionParaEquipo(Equipo equipo) {
        return Clasificacion.builder()
                .equipo(equipo).competicion(competicion).temporada(1)
                .puntos(0).victorias(0).empates(0).derrotas(0)
                .golesFavor(0).golesContra(0).diferenciaGoles(0).partidosJugados(0)
                .build();
    }

    private Evento eventoFinalizado(Long id, int golesLocal, int golesVisitante) {
        return Evento.builder()
                .id(id)
                .competicion(competicion)
                .resultadoLocal(golesLocal)
                .resultadoVisitante(golesVisitante)
                .estado(Evento.EstadoEvento.FINALIZADO)
                .build();
    }

    private EventoEquipo eventoEquipo(Evento evento, Equipo equipo, boolean esLocal) {
        return EventoEquipo.builder()
                .evento(evento).equipo(equipo).esLocal(esLocal).build();
    }
}
