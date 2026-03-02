package com.compapption.api.it;

import com.compapption.api.dto.auth.AuthResponse;
import com.compapption.api.dto.auth.LoginRequest;
import com.compapption.api.dto.auth.RegistroRequest;
import com.compapption.api.entity.Competicion;
import com.compapption.api.entity.Deporte;
import com.compapption.api.entity.Rol;
import com.compapption.api.repository.*;
import com.compapption.api.request.invitacion.InvitacionCreateRequest;
import com.compapption.api.util.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración del flujo de invitaciones.
 * Verifica creación, aceptación y rechazo de invitaciones vía API.
 *
 * Nota: InvitacionDetalleDTO no expone el token (se envía por email que está mockeado).
 * Para los tests de aceptar/rechazar se recupera el token directamente del repositorio.
 */
class FlujoInvitacionIT extends BaseIntegrationTest {

    @Autowired private DeporteRepository deporteRepository;
    @Autowired private CompeticionRepository competicionRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private UsuarioRolCompeticionRepository urcRepository;
    @Autowired private InvitacionRepository invitacionRepository;

    private final String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

    private Long emisorId;
    private String emisorToken;
    private Long destinatarioId;
    private String destinatarioToken;
    private Long competicionId;

    @BeforeEach
    void setUp() {
        // Crear roles si no existen (pueden existir de otros tests sin rollback)
        ensureRolExists(Rol.RolNombre.JUGADOR);
        ensureRolExists(Rol.RolNombre.ADMIN_COMPETICION);

        // Crear deporte via JPA
        Deporte deporte = deporteRepository.save(
                Deporte.builder().nombre("Baloncesto-" + suffix).build());

        // Registrar emisor (quien crea la invitación)
        RegistroRequest regEmisor = RegistroRequest.builder()
                .username("emisor_" + suffix)
                .email("emisor_" + suffix + "@test.com")
                .password("password123")
                .build();
        ResponseEntity<AuthResponse> regResp = restTemplate.postForEntity(
                url("/auth/registro"), regEmisor, AuthResponse.class);
        emisorId = regResp.getBody().getUsuario().getId();

        // Login emisor
        LoginRequest loginEmisor = LoginRequest.builder()
                .usernameOrEmail("emisor_" + suffix)
                .password("password123")
                .build();
        ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity(
                url("/auth/login"), loginEmisor, AuthResponse.class);
        emisorToken = loginResp.getBody().getAccessToken();

        // Crear competición via JPA
        competicionId = competicionRepository.save(
                Competicion.builder()
                        .nombre("Torneo-" + suffix)
                        .deporte(deporte)
                        .creador(usuarioRepository.findById(emisorId).orElseThrow())
                        .build()
        ).getId();

        // Asignar ADMIN_COMPETICION al emisor para que pueda crear invitaciones
        Rol rolAdmin = rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPETICION).orElseThrow();
        Competicion competicion = competicionRepository.findById(competicionId).orElseThrow();
        com.compapption.api.entity.Usuario emisor = usuarioRepository.findById(emisorId).orElseThrow();
        urcRepository.save(com.compapption.api.entity.UsuarioRolCompeticion.builder()
                .usuario(emisor).competicion(competicion).rol(rolAdmin).build());

        // Registrar destinatario
        RegistroRequest regDest = RegistroRequest.builder()
                .username("dest_" + suffix)
                .email("dest_" + suffix + "@test.com")
                .password("password123")
                .build();
        ResponseEntity<AuthResponse> destResp = restTemplate.postForEntity(
                url("/auth/registro"), regDest, AuthResponse.class);
        destinatarioId = destResp.getBody().getUsuario().getId();

        // Login destinatario
        LoginRequest loginDest = LoginRequest.builder()
                .usernameOrEmail("dest_" + suffix)
                .password("password123")
                .build();
        ResponseEntity<AuthResponse> destLoginResp = restTemplate.postForEntity(
                url("/auth/login"), loginDest, AuthResponse.class);
        destinatarioToken = destLoginResp.getBody().getAccessToken();
    }

    private void ensureRolExists(Rol.RolNombre nombre) {
        if (!rolRepository.existsByNombre(nombre)) {
            rolRepository.save(Rol.builder().nombre(nombre).build());
        }
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Crea la invitación vía HTTP y devuelve el token real de la BD.
     * InvitacionDetalleDTO no expone el token (se enviaría por email, que está mockeado),
     * así que lo recuperamos directamente por el id de la invitación creada.
     */
    private String crearInvitacion() {
        InvitacionCreateRequest req = InvitacionCreateRequest.builder()
                .destinatarioEmail("dest_" + suffix + "@test.com")
                .competicionId(competicionId)
                .rolOfrecido("ADMIN_COMPETICION")
                .build();
        ResponseEntity<Map> resp = restTemplate.exchange(
                url("/invitaciones?usuarioId=" + emisorId),
                HttpMethod.POST,
                new HttpEntity<>(req, authHeaders(emisorToken)),
                Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long invitacionId = ((Number) resp.getBody().get("id")).longValue();
        // El token no viene en el DTO — lo leemos directamente del repositorio
        return invitacionRepository.findById(invitacionId).orElseThrow().getToken();
    }

    // =========================================================
    // Crear invitación
    // =========================================================

    @Test
    void crearInvitacion_autenticado_devuelve201ConId() {
        InvitacionCreateRequest req = InvitacionCreateRequest.builder()
                .destinatarioEmail("dest_" + suffix + "@test.com")
                .competicionId(competicionId)
                .rolOfrecido("ADMIN_COMPETICION")
                .build();

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/invitaciones?usuarioId=" + emisorId),
                HttpMethod.POST,
                new HttpEntity<>(req, authHeaders(emisorToken)),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("id");
        assertThat(response.getBody().get("estado")).isEqualTo("PENDIENTE");
    }

    @Test
    void crearInvitacion_sinAuth_devuelve401() {
        InvitacionCreateRequest req = InvitacionCreateRequest.builder()
                .destinatarioEmail("dest_" + suffix + "@test.com")
                .competicionId(competicionId)
                .rolOfrecido("ADMIN_COMPETICION")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/invitaciones?usuarioId=" + emisorId), req, String.class);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    // =========================================================
    // Aceptar invitación → rol asignado al destinatario
    // =========================================================

    @Test
    void aceptarInvitacion_tokenValido_asignaRolAlDestinatario() {
        String token = crearInvitacion();

        ResponseEntity<Map> aceptarResp = restTemplate.exchange(
                url("/invitaciones/" + token + "/aceptar?usuarioId=" + destinatarioId),
                HttpMethod.PUT,
                new HttpEntity<>(null, authHeaders(destinatarioToken)),
                Map.class);

        assertThat(aceptarResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verificar que el rol fue asignado
        boolean tieneRol = urcRepository.existsByUsuarioIdAndCompeticionId(
                destinatarioId, competicionId);
        assertThat(tieneRol).isTrue();
    }

    // =========================================================
    // Rechazar invitación → rol NO asignado
    // =========================================================

    @Test
    void rechazarInvitacion_tokenValido_noAsignaRolAdicional() {
        String token = crearInvitacion();

        ResponseEntity<Map> rechazarResp = restTemplate.exchange(
                url("/invitaciones/" + token + "/rechazar?usuarioId=" + destinatarioId),
                HttpMethod.PUT,
                new HttpEntity<>(null, authHeaders(destinatarioToken)),
                Map.class);

        assertThat(rechazarResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verificar que el destinatario NO tiene rol (no se le asignó)
        boolean tieneRol = urcRepository.existsByUsuarioIdAndCompeticionId(
                destinatarioId, competicionId);
        assertThat(tieneRol).isFalse();
    }
}
