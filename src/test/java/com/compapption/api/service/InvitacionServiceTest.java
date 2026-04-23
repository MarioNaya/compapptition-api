package com.compapption.api.service;

import com.compapption.api.entity.*;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.InvitacionMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.invitacion.InvitacionCreateRequest;
import com.compapption.api.service.log.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitacionServiceTest {

    @Mock private InvitacionRepository invitacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CompeticionRepository competicionRepository;
    @Mock private EquipoRepository equipoRepository;
    @Mock private RolRepository rolRepository;
    @Mock private UsuarioRolCompeticionRepository usuarioRolCompeticionRepository;
    @Mock private EquipoManagerRepository equipoManagerRepository;
    @Mock private EquipoJugadorRepository equipoJugadorRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private EmailService emailService;
    @Mock private InvitacionMapper invitacionMapper;
    @Mock private LogService logService;
    @Mock private NotificacionService notificacionService;

    @InjectMocks private InvitacionService invitacionService;

    private Usuario emisor;
    private Usuario receptor;
    private Competicion competicion;
    private Equipo equipo;

    @BeforeEach
    void setUp() {
        emisor = Usuario.builder().id(1L).username("admin").email("admin@test.com").build();
        receptor = Usuario.builder().id(2L).username("receptor").email("receptor@test.com").nombre("Receptor").apellidos("Test").build();
        competicion = Competicion.builder().id(10L).nombre("Liga Test").build();
        equipo = Equipo.builder().id(20L).nombre("Equipo Test").build();
    }

    // =========================================================
    // crearInvitacion() — validaciones de rol
    // =========================================================

    @Test
    void crearInvitacion_rolInvalido_lanzaBadRequest() {
        InvitacionCreateRequest req = new InvitacionCreateRequest();
        req.setRolOfrecido("ROL_INEXISTENTE");
        req.setDestinatarioEmail("alguien@test.com");

        assertThatThrownBy(() -> invitacionService.crearInvitacion(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Rol ofrecido no válido");
    }

    @Test
    void crearInvitacion_adminCompeticionSinCompeticionId_lanzaBadRequest() {
        InvitacionCreateRequest req = new InvitacionCreateRequest();
        req.setRolOfrecido("ADMIN_COMPETICION");
        req.setDestinatarioEmail("alguien@test.com");
        // competicionId = null

        assertThatThrownBy(() -> invitacionService.crearInvitacion(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("competicion");
    }

    @Test
    void crearInvitacion_jugadorSinEquipoId_lanzaBadRequest() {
        InvitacionCreateRequest req = new InvitacionCreateRequest();
        req.setRolOfrecido("JUGADOR");
        req.setDestinatarioEmail("alguien@test.com");
        req.setCompeticionId(10L);
        // equipoId = null

        assertThatThrownBy(() -> invitacionService.crearInvitacion(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("equipo");
    }

    @Test
    void crearInvitacion_managerEquipoSinEquipoId_lanzaBadRequest() {
        InvitacionCreateRequest req = new InvitacionCreateRequest();
        req.setRolOfrecido("MANAGER_EQUIPO");
        req.setDestinatarioEmail("alguien@test.com");
        req.setCompeticionId(10L);
        // equipoId = null

        assertThatThrownBy(() -> invitacionService.crearInvitacion(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("equipo");
    }

    // =========================================================
    // crearInvitacion() — duplicados
    // =========================================================

    @Test
    void crearInvitacion_duplicadoPendienteEnCompeticion_lanzaBadRequest() {
        InvitacionCreateRequest req = buildRequest("ADMIN_COMPETICION", "test@test.com", 10L, null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(emisor));
        when(competicionRepository.findById(10L)).thenReturn(Optional.of(competicion));
        when(invitacionRepository.existsByDestinatarioEmailAndCompeticionIdAndEstado(
                "test@test.com", 10L, Invitacion.EstadoInvitacion.PENDIENTE)).thenReturn(true);

        assertThatThrownBy(() -> invitacionService.crearInvitacion(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("invitación pendiente");
    }

    // =========================================================
    // crearInvitacion() — flujo feliz
    // =========================================================

    @Test
    void crearInvitacion_adminCompeticion_guardaInvitacionYEnviaEmail() {
        InvitacionCreateRequest req = buildRequest("ADMIN_COMPETICION", "nuevo@test.com", 10L, null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(emisor));
        when(competicionRepository.findById(10L)).thenReturn(Optional.of(competicion));
        when(invitacionRepository.existsByDestinatarioEmailAndCompeticionIdAndEstado(
                anyString(), anyLong(), any(Invitacion.EstadoInvitacion.class)))
                .thenReturn(false);
        when(usuarioRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.empty());
        Invitacion saved = Invitacion.builder().id(100L).competicion(competicion)
                .rolOfrecido("ADMIN_COMPETICION").build();
        when(invitacionRepository.save(any())).thenReturn(saved);
        when(invitacionMapper.toDetalleDTO(any())).thenReturn(null);

        invitacionService.crearInvitacion(1L, req);

        ArgumentCaptor<Invitacion> captor = ArgumentCaptor.forClass(Invitacion.class);
        verify(invitacionRepository).save(captor.capture());
        Invitacion creada = captor.getValue();

        assertThat(creada.getDestinatarioEmail()).isEqualTo("nuevo@test.com");
        assertThat(creada.getRolOfrecido()).isEqualTo("ADMIN_COMPETICION");
        assertThat(creada.getToken()).isNotBlank();
        assertThat(creada.getFechaExpiracion()).isAfter(LocalDateTime.now());
        verify(emailService).enviarInvitacion(eq("nuevo@test.com"), any(), any(), eq("ADMIN_COMPETICION"), anyString());
    }

    @Test
    void crearInvitacion_destinatarioYaRegistrado_vinculaDestinatario() {
        InvitacionCreateRequest req = buildRequest("ADMIN_COMPETICION", receptor.getEmail(), 10L, null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(emisor));
        when(competicionRepository.findById(10L)).thenReturn(Optional.of(competicion));
        when(invitacionRepository.existsByDestinatarioEmailAndCompeticionIdAndEstado(
                anyString(), anyLong(), any(Invitacion.EstadoInvitacion.class)))
                .thenReturn(false);
        when(usuarioRepository.findByEmail(receptor.getEmail())).thenReturn(Optional.of(receptor));
        when(invitacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invitacionMapper.toDetalleDTO(any())).thenReturn(null);

        invitacionService.crearInvitacion(1L, req);

        ArgumentCaptor<Invitacion> captor = ArgumentCaptor.forClass(Invitacion.class);
        verify(invitacionRepository).save(captor.capture());
        assertThat(captor.getValue().getDestinatario()).isEqualTo(receptor);
    }

    // =========================================================
    // aceptarPorToken() — validaciones
    // =========================================================

    @Test
    void aceptarPorToken_tokenNoExiste_lanzaResourceNotFound() {
        when(invitacionRepository.findByToken("token-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitacionService.aceptarPorToken("token-inexistente", 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void aceptarPorToken_invitacionYaAceptada_lanzaBadRequest() {
        Invitacion invitacion = invitacionConEstado(Invitacion.EstadoInvitacion.ACEPTADA, "ADMIN_COMPETICION");
        when(invitacionRepository.findByToken("tok")).thenReturn(Optional.of(invitacion));

        assertThatThrownBy(() -> invitacionService.aceptarPorToken("tok", 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no está pendiente");
    }

    @Test
    void aceptarPorToken_invitacionExpirada_marcaExpiradaYLanzaBadRequest() {
        Invitacion invitacion = invitacionConEstado(Invitacion.EstadoInvitacion.PENDIENTE, "ADMIN_COMPETICION");
        invitacion.setFechaExpiracion(LocalDateTime.now().minusDays(1)); // ya expirada
        when(invitacionRepository.findByToken("tok")).thenReturn(Optional.of(invitacion));
        when(invitacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> invitacionService.aceptarPorToken("tok", 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expirado");

        assertThat(invitacion.getEstado()).isEqualTo(Invitacion.EstadoInvitacion.EXPIRADA);
    }

    // =========================================================
    // aceptarPorToken() — flujo ADMIN_COMPETICION
    // =========================================================

    @Test
    void aceptarPorToken_adminCompeticion_asignaRolEnCompeticion() {
        Invitacion invitacion = invitacionConEstado(Invitacion.EstadoInvitacion.PENDIENTE, "ADMIN_COMPETICION");
        invitacion.setCompeticion(competicion);

        Rol rol = Rol.builder().id(1L).nombre(Rol.RolNombre.ADMIN_COMPETICION).build();

        when(invitacionRepository.findByToken("tok")).thenReturn(Optional.of(invitacion));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(usuarioRolCompeticionRepository.existsByUsuarioIdAndCompeticionIdAndRolNombre(
                2L, 10L, Rol.RolNombre.ADMIN_COMPETICION)).thenReturn(false);
        when(rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPETICION)).thenReturn(Optional.of(rol));
        when(invitacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invitacionMapper.toDetalleDTO(any())).thenReturn(null);

        invitacionService.aceptarPorToken("tok", 2L);

        verify(usuarioRolCompeticionRepository).save(any(UsuarioRolCompeticion.class));
        assertThat(invitacion.getEstado()).isEqualTo(Invitacion.EstadoInvitacion.ACEPTADA);
        assertThat(invitacion.getDestinatario()).isEqualTo(receptor);
    }

    // =========================================================
    // aceptarPorToken() — flujo JUGADOR (crea jugador si no existe)
    // =========================================================

    @Test
    void aceptarPorToken_jugadorSinPerfilPrevio_creaJugadorAutomaticamente() {
        Invitacion invitacion = invitacionConEstado(Invitacion.EstadoInvitacion.PENDIENTE, "JUGADOR");
        invitacion.setEquipo(equipo);

        Jugador jugadorCreado = Jugador.builder().id(50L).nombre("Receptor").build();

        when(invitacionRepository.findByToken("tok")).thenReturn(Optional.of(invitacion));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(jugadorRepository.findByUsuarioId(2L)).thenReturn(Optional.empty()); // no existe
        when(jugadorRepository.save(any())).thenReturn(jugadorCreado);
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(20L, 50L)).thenReturn(false);
        when(invitacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invitacionMapper.toDetalleDTO(any())).thenReturn(null);

        invitacionService.aceptarPorToken("tok", 2L);

        verify(jugadorRepository).save(any(Jugador.class));
        verify(equipoJugadorRepository).save(any(EquipoJugador.class));
        assertThat(invitacion.getEstado()).isEqualTo(Invitacion.EstadoInvitacion.ACEPTADA);
    }

    @Test
    void aceptarPorToken_jugadorConPerfilPrevio_reutilizaJugador() {
        Invitacion invitacion = invitacionConEstado(Invitacion.EstadoInvitacion.PENDIENTE, "JUGADOR");
        invitacion.setEquipo(equipo);

        Jugador jugadorExistente = Jugador.builder().id(50L).nombre("Receptor").build();

        when(invitacionRepository.findByToken("tok")).thenReturn(Optional.of(invitacion));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(jugadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(jugadorExistente));
        when(equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(20L, 50L)).thenReturn(false);
        when(invitacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invitacionMapper.toDetalleDTO(any())).thenReturn(null);

        invitacionService.aceptarPorToken("tok", 2L);

        verify(jugadorRepository, never()).save(any()); // no crea jugador nuevo
        verify(equipoJugadorRepository).save(any(EquipoJugador.class));
    }

    // =========================================================
    // rechazarPorToken()
    // =========================================================

    @Test
    void rechazarPorToken_invitacionPendiente_cambiEstadoARechazada() {
        Invitacion invitacion = invitacionConEstado(Invitacion.EstadoInvitacion.PENDIENTE, "ADMIN_COMPETICION");
        invitacion.setCompeticion(competicion);

        when(invitacionRepository.findByToken("tok")).thenReturn(Optional.of(invitacion));
        when(usuarioRepository.getReferenceById(2L)).thenReturn(receptor);
        when(invitacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invitacionMapper.toDetalleDTO(any())).thenReturn(null);

        invitacionService.rechazarPorToken("tok", 2L);

        assertThat(invitacion.getEstado()).isEqualTo(Invitacion.EstadoInvitacion.RECHAZADA);
    }

    @Test
    void rechazarPorToken_invitacionYaAceptada_lanzaBadRequest() {
        Invitacion invitacion = invitacionConEstado(Invitacion.EstadoInvitacion.ACEPTADA, "ADMIN_COMPETICION");
        when(invitacionRepository.findByToken("tok")).thenReturn(Optional.of(invitacion));

        assertThatThrownBy(() -> invitacionService.rechazarPorToken("tok", 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no está pendiente");
    }

    // =========================================================
    // Helpers
    // =========================================================

    private InvitacionCreateRequest buildRequest(String rol, String email, Long competicionId, Long equipoId) {
        InvitacionCreateRequest req = new InvitacionCreateRequest();
        req.setRolOfrecido(rol);
        req.setDestinatarioEmail(email);
        req.setCompeticionId(competicionId);
        req.setEquipoId(equipoId);
        return req;
    }

    private Invitacion invitacionConEstado(Invitacion.EstadoInvitacion estado, String rol) {
        return Invitacion.builder()
                .id(99L)
                .emisor(emisor)
                .rolOfrecido(rol)
                .estado(estado)
                .token("tok")
                .destinatarioEmail("dest@test.com")
                .fechaExpiracion(LocalDateTime.now().plusDays(7))
                .build();
    }
}
