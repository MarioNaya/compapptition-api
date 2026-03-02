package com.compapption.api.service;

import com.compapption.api.entity.*;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.CompeticionMapper;
import com.compapption.api.mapper.EquipoMapper;
import com.compapption.api.repository.*;
import com.compapption.api.service.log.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompeticionServiceTest {

    @Mock private CompeticionRepository competicionRepository;
    @Mock private DeporteRepository deporteRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CompeticionEquipoRepository competicionEquipoRepository;
    @Mock private EquipoRepository equipoRepository;
    @Mock private CompeticionMapper competicionMapper;
    @Mock private EquipoMapper equipoMapper;
    @Mock private ClasificacionService clasificacionService;
    @Mock private ConfiguracionCompeticionService configuracionCompeticionService;
    @Mock private UsuarioRolCompeticionService usuarioRolCompeticionService;
    @Mock private LogService logService;

    @InjectMocks private CompeticionService competicionService;

    private Usuario creador;
    private Equipo equipo;
    private Competicion competicion;

    @BeforeEach
    void setUp() {
        creador = Usuario.builder().id(1L).username("admin").build();
        equipo = Equipo.builder().id(10L).nombre("Test FC").build();
        competicion = Competicion.builder()
                .id(5L).nombre("Liga Test")
                .creador(creador)
                .temporadaActual(1)
                .estado(Competicion.EstadoCompeticion.BORRADOR)
                .inscripcionAbierta(true)
                .build();
    }

    // =========================================================
    // eliminar() — validaciones
    // =========================================================

    @Test
    void eliminar_competicionNoExiste_lanzaResourceNotFound() {
        when(competicionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competicionService.eliminar(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminar_competicionActiva_lanzaBadRequest() {
        Competicion activa = Competicion.builder()
                .id(5L).nombre("Activa").creador(creador)
                .estado(Competicion.EstadoCompeticion.ACTIVA)
                .build();

        when(competicionRepository.findById(5L)).thenReturn(Optional.of(activa));

        // creadorId = 1L = activa.getCreador().getId() → pasa validarPermisoEdicion
        assertThatThrownBy(() -> competicionService.eliminar(5L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("activa");
    }

    @Test
    void eliminar_borrador_eliminaCorrectamente() {
        when(competicionRepository.findById(5L)).thenReturn(Optional.of(competicion));

        competicionService.eliminar(5L, 1L); // usuarioId = creadorId = 1L

        verify(competicionRepository).delete(competicion);
    }

    // =========================================================
    // altaEquipo() — validaciones
    // =========================================================

    @Test
    void altaEquipo_competicionNoExiste_lanzaResourceNotFound() {
        when(competicionRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> competicionService.altaEquipo(99L, 10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void altaEquipo_equipoYaInscrito_lanzaBadRequest() {
        when(competicionRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(competicion));
        when(equipoRepository.findById(10L)).thenReturn(Optional.of(equipo));
        when(competicionEquipoRepository.existsByCompeticionIdAndEquipoId(5L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> competicionService.altaEquipo(5L, 10L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inscrito");
    }

    @Test
    void altaEquipo_inscripcionCerrada_lanzaBadRequest() {
        Competicion cerrada = Competicion.builder()
                .id(5L).nombre("Cerrada").creador(creador)
                .inscripcionAbierta(false)
                .build();

        when(competicionRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(cerrada));
        when(equipoRepository.findById(10L)).thenReturn(Optional.of(equipo));
        when(competicionEquipoRepository.existsByCompeticionIdAndEquipoId(5L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> competicionService.altaEquipo(5L, 10L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cerradas");
    }

    @Test
    void altaEquipo_flujoFeliz_guardaYInicializaClasificacion() {
        when(competicionRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(competicion));
        when(equipoRepository.findById(10L)).thenReturn(Optional.of(equipo));
        when(competicionEquipoRepository.existsByCompeticionIdAndEquipoId(5L, 10L)).thenReturn(false);
        when(competicionEquipoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        competicionService.altaEquipo(5L, 10L, 1L);

        verify(competicionEquipoRepository).save(any(CompeticionEquipo.class));
        verify(clasificacionService).inicializarClasificacionEquipo(competicion, equipo);
    }

    // =========================================================
    // cambiarTemporada() — validaciones
    // =========================================================

    @Test
    void cambiarTemporada_nuevaTemporadaMenorIgualActual_lanzaBadRequest() {
        // temporadaActual = 1 en el setUp
        when(competicionRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(competicion));

        assertThatThrownBy(() -> competicionService.cambiarTemporada(5L, 1, 1L)) // nueva=1 = actual=1
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("mayor");
    }

    @Test
    void cambiarTemporada_nuevaTemporadaMayor_actualizaYInicializaClasificaciones() {
        when(competicionRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(competicion));
        when(competicionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(competicionEquipoRepository.findActivosByCompeticionId(5L)).thenReturn(List.of());
        when(competicionMapper.toDetalleDTO(any())).thenReturn(null);

        competicionService.cambiarTemporada(5L, 2, 1L);

        verify(competicionRepository).save(competicion);
    }

    // =========================================================
    // cambiarEstado() → validarCambioEstado() — mínimo de equipos
    // =========================================================

    @Test
    void cambiarEstado_ligaConMenosDe3Equipos_lanzaBadRequest() {
        ConfiguracionCompeticion config = ConfiguracionCompeticion.builder()
                .formato(ConfiguracionCompeticion.FormatoCompeticion.LIGA)
                .build();
        Competicion competicionConConfig = Competicion.builder()
                .id(5L).nombre("Liga").creador(creador)
                .configuracion(config)
                .build();

        when(competicionRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(competicionConConfig));
        when(competicionEquipoRepository.countActivosByCompeticionId(5L)).thenReturn(2L); // solo 2

        assertThatThrownBy(() -> competicionService.cambiarEstado(
                5L, Competicion.EstadoCompeticion.ACTIVA, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("3 equipos");
    }

    @Test
    void cambiarEstado_playoffCon2Equipos_activaCorrectamente() {
        ConfiguracionCompeticion config = ConfiguracionCompeticion.builder()
                .formato(ConfiguracionCompeticion.FormatoCompeticion.PLAYOFF)
                .build();
        Competicion playoffComp = Competicion.builder()
                .id(5L).nombre("Playoff").creador(creador)
                .configuracion(config)
                .estado(Competicion.EstadoCompeticion.BORRADOR)
                .build();

        when(competicionRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(playoffComp));
        when(competicionEquipoRepository.countActivosByCompeticionId(5L)).thenReturn(4L); // ≥ 2
        when(competicionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(competicionMapper.toDetalleDTO(any())).thenReturn(null);

        // No debe lanzar excepción
        competicionService.cambiarEstado(5L, Competicion.EstadoCompeticion.ACTIVA, 1L);

        verify(competicionRepository).save(playoffComp);
    }
}
