package com.compapption.api.service;

import com.compapption.api.entity.*;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.EstadisticaMapper;
import com.compapption.api.mapper.EventoMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.evento.EventoCreateRequest;
import com.compapption.api.request.evento.ResultadoRequest;
import com.compapption.api.service.log.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock private EventoRepository eventoRepository;
    @Mock private CompeticionRepository competicionRepository;
    @Mock private EquipoRepository equipoRepository;
    @Mock private EventoEquipoRepository eventoEquipoRepository;
    @Mock private EstadisticaJugadorEventoRepository estadisticaJugadorEventoRepository;
    @Mock private TipoEstadisticaRepository tipoEstadisticaRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private EventoMapper eventoMapper;
    @Mock private EstadisticaMapper estadisticaMapper;
    @Mock private ClasificacionService clasificacionService;
    @Mock private LogService logService;

    @InjectMocks private EventoService eventoService;

    private Competicion competicion;
    private Equipo local;
    private Equipo visitante;

    @BeforeEach
    void setUp() {
        competicion = Competicion.builder().id(1L).nombre("Liga Test").temporadaActual(1).build();
        local = Equipo.builder().id(1L).nombre("Local FC").build();
        visitante = Equipo.builder().id(2L).nombre("Visitante FC").build();
    }

    // =========================================================
    // registrarResultado() — validaciones
    // =========================================================

    @Test
    void registrarResultado_eventoNoExiste_lanzaResourceNotFound() {
        when(eventoRepository.findByIdWithEquipos(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.registrarResultado(99L, resultado(2, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registrarResultado_eventoYaFinalizado_lanzaBadRequest() {
        Evento evento = eventoConEstado(10L, Evento.EstadoEvento.FINALIZADO);
        when(eventoRepository.findByIdWithEquipos(10L)).thenReturn(Optional.of(evento));

        assertThatThrownBy(() -> eventoService.registrarResultado(10L, resultado(1, 0)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("finalizado");
    }

    // =========================================================
    // registrarResultado() — resultado guardado y clasificación recalculada
    // =========================================================

    @Test
    void registrarResultado_flujoFeliz_actualizaResultadoYEstadoYLlamaClasificacion() {
        Evento evento = eventoConEquipos(10L, local, visitante);
        when(eventoRepository.findByIdWithEquipos(10L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // findByPartidoAnteriorId no se stubbea: Mockito devuelve List vacía por defecto
        when(eventoMapper.toResultadoDTO(any())).thenReturn(null);

        eventoService.registrarResultado(10L, resultado(3, 1));

        assertThat(evento.getResultadoLocal()).isEqualTo(3);
        assertThat(evento.getResultadoVisitante()).isEqualTo(1);
        assertThat(evento.getEstado()).isEqualTo(Evento.EstadoEvento.FINALIZADO);
        verify(clasificacionService).calcularClasificacion(1L);
    }

    // =========================================================
    // registrarResultado() — avance playoff (partido único)
    // =========================================================

    @Test
    void registrarResultado_localGana_guardaEventoEquipoLocalEnSiguienteRonda() {
        Evento evento = eventoConEquipos(10L, local, visitante);

        // La siguiente ronda apunta al evento como anteriorLocal → ganador juega de local
        Evento siguienteRonda = Evento.builder().id(20L)
                .partidoAnteriorLocal(evento)
                .build();

        when(eventoRepository.findByIdWithEquipos(10L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepository.findByPartidoAnteriorId(10L)).thenReturn(List.of(siguienteRonda));
        when(eventoEquipoRepository.findByEventoId(20L)).thenReturn(List.of());
        when(eventoMapper.toResultadoDTO(any())).thenReturn(null);

        eventoService.registrarResultado(10L, resultado(2, 1)); // local gana

        ArgumentCaptor<EventoEquipo> captor = ArgumentCaptor.forClass(EventoEquipo.class);
        verify(eventoEquipoRepository).save(captor.capture());
        assertThat(captor.getValue().getEquipo()).isEqualTo(local);
        assertThat(captor.getValue().isEsLocal()).isTrue();
    }

    @Test
    void registrarResultado_visitanteGana_guardaEventoEquipoVisitanteEnSiguienteRonda() {
        Evento evento = eventoConEquipos(10L, local, visitante);

        // La siguiente ronda apunta al evento como anteriorVisitante → ganador juega de visitante
        Evento siguienteRonda = Evento.builder().id(20L)
                .partidoAnteriorVisitante(evento)
                // anteriorLocal = null → esLocal = false
                .build();

        when(eventoRepository.findByIdWithEquipos(10L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepository.findByPartidoAnteriorId(10L)).thenReturn(List.of(siguienteRonda));
        when(eventoEquipoRepository.findByEventoId(20L)).thenReturn(List.of());
        when(eventoMapper.toResultadoDTO(any())).thenReturn(null);

        eventoService.registrarResultado(10L, resultado(0, 3)); // visitante gana

        ArgumentCaptor<EventoEquipo> captor = ArgumentCaptor.forClass(EventoEquipo.class);
        verify(eventoEquipoRepository).save(captor.capture());
        assertThat(captor.getValue().getEquipo()).isEqualTo(visitante);
        assertThat(captor.getValue().isEsLocal()).isFalse();
    }

    @Test
    void registrarResultado_empate_noAvanzaNingúnEquipo() {
        // Empate → determinarGanadorPartidoUnico devuelve empty → no se llama a findByPartidoAnteriorId
        Evento evento = eventoConEstado(10L, Evento.EstadoEvento.PROGRAMADO);
        when(eventoRepository.findByIdWithEquipos(10L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoMapper.toResultadoDTO(any())).thenReturn(null);

        eventoService.registrarResultado(10L, resultado(1, 1)); // empate

        verify(eventoEquipoRepository, never()).save(any(EventoEquipo.class));
    }

    @Test
    void registrarResultado_ganadorYaRegistradoEnSiguienteRonda_noGuardaDuplicado() {
        Evento evento = eventoConEquipos(10L, local, visitante);
        Evento siguienteRonda = Evento.builder().id(20L).partidoAnteriorLocal(evento).build();

        // El ganador ya existe en la siguiente ronda
        EventoEquipo eeExistente = EventoEquipo.builder().equipo(local).esLocal(true).build();

        when(eventoRepository.findByIdWithEquipos(10L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepository.findByPartidoAnteriorId(10L)).thenReturn(List.of(siguienteRonda));
        when(eventoEquipoRepository.findByEventoId(20L)).thenReturn(List.of(eeExistente)); // ya existe
        when(eventoMapper.toResultadoDTO(any())).thenReturn(null);

        eventoService.registrarResultado(10L, resultado(2, 1));

        // No se guarda duplicado
        verify(eventoEquipoRepository, never()).save(any(EventoEquipo.class));
    }

    // =========================================================
    // crear() — validaciones
    // =========================================================

    @Test
    void crear_competicionNoExiste_lanzaResourceNotFound() {
        when(competicionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.crear(99L, createRequest(1L, 2L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crear_equipoLocalNoExiste_lanzaResourceNotFound() {
        when(competicionRepository.findById(1L)).thenReturn(Optional.of(competicion));
        when(equipoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.crear(1L, createRequest(99L, 2L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crear_mismoEquipoLocalYVisitante_lanzaBadRequest() {
        when(competicionRepository.findById(1L)).thenReturn(Optional.of(competicion));
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(local));

        assertThatThrownBy(() -> eventoService.crear(1L, createRequest(1L, 1L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("mismo");
    }

    // =========================================================
    // crear() — flujo feliz
    // =========================================================

    @Test
    void crear_flujoFeliz_guardaEventoYDosEventoEquipos() {
        when(competicionRepository.findById(1L)).thenReturn(Optional.of(competicion));
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(local));
        when(equipoRepository.findById(2L)).thenReturn(Optional.of(visitante));
        Evento eventoGuardado = Evento.builder().id(10L).competicion(competicion).build();
        when(eventoRepository.save(any())).thenReturn(eventoGuardado);
        when(eventoMapper.toDetalleDTO(any())).thenReturn(null);

        eventoService.crear(1L, createRequest(1L, 2L));

        verify(eventoRepository).save(any(Evento.class));

        ArgumentCaptor<EventoEquipo> captor = ArgumentCaptor.forClass(EventoEquipo.class);
        verify(eventoEquipoRepository, times(2)).save(captor.capture());

        List<EventoEquipo> guardados = captor.getAllValues();
        assertThat(guardados).hasSize(2);
        assertThat(guardados.stream().anyMatch(EventoEquipo::isEsLocal)).isTrue();
        assertThat(guardados.stream().anyMatch(ee -> !ee.isEsLocal())).isTrue();
    }

    // =========================================================
    // eliminar() — validaciones y flujo feliz
    // =========================================================

    @Test
    void eliminar_eventoNoExiste_lanzaResourceNotFound() {
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminar_eventoFinalizado_lanzaBadRequest() {
        Evento evento = eventoConEstado(10L, Evento.EstadoEvento.FINALIZADO);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        assertThatThrownBy(() -> eventoService.eliminar(10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("eliminar");
    }

    @Test
    void eliminar_eventoProgramado_eliminaCorrectamente() {
        Evento evento = eventoConEstado(10L, Evento.EstadoEvento.PROGRAMADO);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        eventoService.eliminar(10L);

        verify(eventoRepository).delete(evento);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Evento eventoConEstado(Long id, Evento.EstadoEvento estado) {
        return Evento.builder().id(id).estado(estado).competicion(competicion).build();
    }

    private Evento eventoConEquipos(Long id, Equipo equipoLocal, Equipo equipoVisitante) {
        EventoEquipo eeLocal = EventoEquipo.builder().equipo(equipoLocal).esLocal(true).build();
        EventoEquipo eeVisitante = EventoEquipo.builder().equipo(equipoVisitante).esLocal(false).build();
        Set<EventoEquipo> equipos = new HashSet<>();
        equipos.add(eeLocal);
        equipos.add(eeVisitante);
        return Evento.builder()
                .id(id)
                .estado(Evento.EstadoEvento.PROGRAMADO)
                .competicion(competicion)
                .equipos(equipos)
                .build();
    }

    private ResultadoRequest resultado(int golesLocal, int golesVisitante) {
        ResultadoRequest r = new ResultadoRequest();
        r.setResultadoLocal(golesLocal);
        r.setResultadoVisitante(golesVisitante);
        return r;
    }

    private EventoCreateRequest createRequest(Long localId, Long visitanteId) {
        EventoCreateRequest r = new EventoCreateRequest();
        r.setEquipoLocalId(localId);
        r.setEquipoVisitanteId(visitanteId);
        r.setFechaHora(LocalDateTime.now().plusDays(7));
        return r;
    }
}
