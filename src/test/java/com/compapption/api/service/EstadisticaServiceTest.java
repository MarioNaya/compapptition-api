package com.compapption.api.service;

import com.compapption.api.dto.estadisticaDTO.EstadisticaAcumuladaDTO;
import com.compapption.api.dto.estadisticaDTO.EstadisticaJugadorDTO;
import com.compapption.api.entity.*;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.exception.UnauthorizedException;
import com.compapption.api.mapper.EstadisticaMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.estadistica.EstadisticaCreateRequest;
import com.compapption.api.service.log.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadisticaServiceTest {

    @Mock private EstadisticaJugadorEventoRepository estadisticaRepository;
    @Mock private CompeticionRepository competicionRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private TipoEstadisticaRepository tipoEstadisticaRepository;
    @Mock private EventoEquipoRepository eventoEquipoRepository;
    @Mock private EquipoJugadorRepository equipoJugadorRepository;
    @Mock private UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;
    @Mock private EquipoManagerRepository equipoManagerRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EstadisticaMapper estadisticaMapper;
    @Mock private LogService logService;

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
    // registrarEstadistica() — POST /estadisticas
    // =========================================================

    @Test
    void registrar_eventoNoExiste_lanzaResourceNotFound() {
        EstadisticaCreateRequest req = buildCreateRequest(99L, 1L, 10L, 2.0);
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estadisticaService.registrarEstadistica(req, 5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(estadisticaRepository, never()).save(any());
        verify(logService, never()).registrar(anyString(), anyLong(), any(), any(), any(), anyLong());
    }

    @Test
    void registrar_jugadorNoInscritoEnEquipos_lanzaBadRequest() {
        // Dos equipos del evento; jugador no activo en ninguno
        Deporte futbol = Deporte.builder().id(100L).nombre("Fútbol").build();
        Competicion comp = competicionConDeporte(1L, futbol);
        Evento evento = eventoConCompeticion(50L, comp);
        TipoEstadistica tipo = TipoEstadistica.builder()
                .id(10L).nombre("Goles").deporte(futbol)
                .tipoValor(TipoEstadistica.TipoValor.ENTERO).build();

        EstadisticaCreateRequest req = buildCreateRequest(50L, 1L, 10L, 2.0);

        when(eventoRepository.findById(50L)).thenReturn(Optional.of(evento));
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(eventoEquipoRepository.findByEventoId(50L)).thenReturn(List.of(
                eventoEquipo(evento, equipo(200L), true),
                eventoEquipo(evento, equipo(201L), false)
        ));
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(200L, 1L)).thenReturn(false);
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(201L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> estadisticaService.registrarEstadistica(req, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no está inscrito");

        verify(estadisticaRepository, never()).save(any());
    }

    @Test
    void registrar_tipoDeOtroDeporte_lanzaBadRequest() {
        Deporte futbol = Deporte.builder().id(100L).nombre("Fútbol").build();
        Deporte baloncesto = Deporte.builder().id(101L).nombre("Baloncesto").build();
        Competicion comp = competicionConDeporte(1L, futbol);
        Evento evento = eventoConCompeticion(50L, comp);
        // Tipo pertenece a baloncesto, no al deporte del evento
        TipoEstadistica tipoBasket = TipoEstadistica.builder()
                .id(10L).nombre("Triples").deporte(baloncesto)
                .tipoValor(TipoEstadistica.TipoValor.ENTERO).build();

        EstadisticaCreateRequest req = buildCreateRequest(50L, 1L, 10L, 2.0);

        when(eventoRepository.findById(50L)).thenReturn(Optional.of(evento));
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(eventoEquipoRepository.findByEventoId(50L)).thenReturn(List.of(
                eventoEquipo(evento, equipo(200L), true)
        ));
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(200L, 1L)).thenReturn(true);
        when(tipoEstadisticaRepository.findById(10L)).thenReturn(Optional.of(tipoBasket));

        assertThatThrownBy(() -> estadisticaService.registrarEstadistica(req, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("deporte");

        verify(estadisticaRepository, never()).save(any());
    }

    @Test
    void registrar_usuarioSinPermiso_lanzaUnauthorized() {
        Deporte futbol = Deporte.builder().id(100L).nombre("Fútbol").build();
        Competicion comp = competicionConDeporte(1L, futbol);
        Evento evento = eventoConCompeticion(50L, comp);
        TipoEstadistica tipo = TipoEstadistica.builder()
                .id(10L).nombre("Goles").deporte(futbol)
                .tipoValor(TipoEstadistica.TipoValor.ENTERO).build();
        Usuario usuarioPlano = Usuario.builder().id(5L).esAdminSistema(false).build();

        EstadisticaCreateRequest req = buildCreateRequest(50L, 1L, 10L, 2.0);

        when(eventoRepository.findById(50L)).thenReturn(Optional.of(evento));
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(eventoEquipoRepository.findByEventoId(50L)).thenReturn(List.of(
                eventoEquipo(evento, equipo(200L), true)
        ));
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(200L, 1L)).thenReturn(true);
        when(tipoEstadisticaRepository.findById(10L)).thenReturn(Optional.of(tipo));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuarioPlano));
        when(usuarioRolCompeticionRepository
                .existsByUsuarioIdAndCompeticionIdAndRolNombreIn(eq(5L), eq(1L), any()))
                .thenReturn(false);
        when(equipoManagerRepository.existsByEquipoIdAndCompeticionIdAndUsuarioId(200L, 1L, 5L))
                .thenReturn(false);

        assertThatThrownBy(() -> estadisticaService.registrarEstadistica(req, 5L))
                .isInstanceOf(UnauthorizedException.class);

        verify(estadisticaRepository, never()).save(any());
    }

    @Test
    void registrar_flujoFeliz_creaYDevuelveDTO() {
        Deporte futbol = Deporte.builder().id(100L).nombre("Fútbol").build();
        Competicion comp = competicionConDeporte(1L, futbol);
        Evento evento = eventoConCompeticion(50L, comp);
        TipoEstadistica tipo = TipoEstadistica.builder()
                .id(10L).nombre("Goles").deporte(futbol)
                .tipoValor(TipoEstadistica.TipoValor.ENTERO).build();
        Usuario admin = Usuario.builder().id(5L).esAdminSistema(true).build();

        EstadisticaCreateRequest req = buildCreateRequest(50L, 1L, 10L, 2.0);

        when(eventoRepository.findById(50L)).thenReturn(Optional.of(evento));
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(eventoEquipoRepository.findByEventoId(50L)).thenReturn(List.of(
                eventoEquipo(evento, equipo(200L), true)
        ));
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(200L, 1L)).thenReturn(true);
        when(tipoEstadisticaRepository.findById(10L)).thenReturn(Optional.of(tipo));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(admin));
        when(estadisticaRepository.findByEventoIdAndJugadorIdAndTipoEstadisticaId(50L, 1L, 10L))
                .thenReturn(Optional.empty());

        ArgumentCaptor<EstadisticaJugadorEvento> captor = ArgumentCaptor.forClass(EstadisticaJugadorEvento.class);
        when(estadisticaRepository.save(captor.capture())).thenAnswer(inv -> {
            EstadisticaJugadorEvento e = inv.getArgument(0);
            e.setId(999L);
            return e;
        });
        when(estadisticaMapper.toDTO(any(EstadisticaJugadorEvento.class)))
                .thenReturn(EstadisticaJugadorDTO.builder()
                        .id(999L)
                        .eventoId(50L)
                        .jugadorId(1L)
                        .tipoEstadisticaId(10L)
                        .valor(new BigDecimal("2"))
                        .build());

        EstadisticaJugadorDTO result = estadisticaService.registrarEstadistica(req, 5L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(999L);
        assertThat(captor.getValue().getValor()).isEqualByComparingTo(new BigDecimal("2"));
        verify(logService).registrar(eq("Estadistica"), eq(999L),
                eq(LogModificacion.AccionLog.CREAR), any(), any(), eq(1L));
    }

    @Test
    void registrar_existeEstadisticaPrevia_actualizaUpsert() {
        Deporte futbol = Deporte.builder().id(100L).nombre("Fútbol").build();
        Competicion comp = competicionConDeporte(1L, futbol);
        Evento evento = eventoConCompeticion(50L, comp);
        TipoEstadistica tipo = TipoEstadistica.builder()
                .id(10L).nombre("Goles").deporte(futbol)
                .tipoValor(TipoEstadistica.TipoValor.ENTERO).build();
        Usuario admin = Usuario.builder().id(5L).esAdminSistema(true).build();

        EstadisticaJugadorEvento existente = EstadisticaJugadorEvento.builder()
                .id(500L)
                .evento(evento)
                .jugador(jugador)
                .tipoEstadistica(tipo)
                .valor(new BigDecimal("1"))
                .build();

        EstadisticaCreateRequest req = buildCreateRequest(50L, 1L, 10L, 3.0);

        when(eventoRepository.findById(50L)).thenReturn(Optional.of(evento));
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(eventoEquipoRepository.findByEventoId(50L)).thenReturn(List.of(
                eventoEquipo(evento, equipo(200L), true)
        ));
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(200L, 1L)).thenReturn(true);
        when(tipoEstadisticaRepository.findById(10L)).thenReturn(Optional.of(tipo));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(admin));
        when(estadisticaRepository.findByEventoIdAndJugadorIdAndTipoEstadisticaId(50L, 1L, 10L))
                .thenReturn(Optional.of(existente));
        when(estadisticaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(estadisticaMapper.toDTO(any(EstadisticaJugadorEvento.class)))
                .thenReturn(EstadisticaJugadorDTO.builder().id(500L).build());

        estadisticaService.registrarEstadistica(req, 5L);

        // Se conserva el mismo id y se actualiza el valor a 3
        assertThat(existente.getValor()).isEqualByComparingTo(new BigDecimal("3"));
        verify(logService).registrar(eq("Estadistica"), eq(500L),
                eq(LogModificacion.AccionLog.EDITAR), any(), any(), eq(1L));
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

    private EstadisticaCreateRequest buildCreateRequest(Long eventoId, Long jugadorId, Long tipoId, Double valor) {
        return EstadisticaCreateRequest.builder()
                .eventoId(eventoId)
                .jugadorId(jugadorId)
                .tipoEstadisticaId(tipoId)
                .valor(valor)
                .build();
    }

    private Competicion competicionConDeporte(Long id, Deporte deporte) {
        return Competicion.builder().id(id).deporte(deporte).build();
    }

    private Evento eventoConCompeticion(Long id, Competicion comp) {
        return Evento.builder().id(id).competicion(comp).build();
    }

    private Equipo equipo(Long id) {
        return Equipo.builder().id(id).build();
    }

    private EventoEquipo eventoEquipo(Evento evento, Equipo equipo, boolean esLocal) {
        return EventoEquipo.builder()
                .evento(evento)
                .equipo(equipo)
                .esLocal(esLocal)
                .build();
    }
}
