package com.compapption.api.it;

import com.compapption.api.dto.auth.AuthResponse;
import com.compapption.api.dto.auth.LoginRequest;
import com.compapption.api.dto.auth.RegistroRequest;
import com.compapption.api.entity.Deporte;
import com.compapption.api.repository.DeporteRepository;
import com.compapption.api.request.competicion.CompeticionCreateRequest;
import com.compapption.api.request.equipo.EquipoCreateRequest;
import com.compapption.api.util.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración del flujo completo de gestión de competiciones.
 * Cada test registra su propio usuario y crea sus propios datos con UUIDs únicos.
 */
class FlujoCampeonatoIT extends BaseIntegrationTest {

    @Autowired private DeporteRepository deporteRepository;

    // =========================================================
    // Helpers
    // =========================================================

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    /** Registra un usuario, hace login y devuelve el AuthResponse completo (token + usuario). */
    private AuthResponse registrarYLogin(String suffix) {
        RegistroRequest registro = RegistroRequest.builder()
                .username("admin_" + suffix)
                .email("admin_" + suffix + "@test.com")
                .password("password123")
                .build();
        restTemplate.postForEntity(url("/auth/registro"), registro, String.class);

        LoginRequest login = LoginRequest.builder()
                .usernameOrEmail("admin_" + suffix)
                .password("password123")
                .build();
        ResponseEntity<AuthResponse> resp = restTemplate.postForEntity(
                url("/auth/login"), login, AuthResponse.class);
        return resp.getBody();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Long crearDeporte(String nombre) {
        Deporte deporte = deporteRepository.save(Deporte.builder().nombre(nombre).build());
        return deporte.getId();
    }

    // =========================================================
    // Endpoints públicos
    // =========================================================

    @Test
    void listarDeportes_sinAuth_devuelve200() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/deportes"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // =========================================================
    // Crear competición autenticado
    // =========================================================

    @Test
    void crearCompeticion_autenticado_devuelve201() {
        String suffix = uniqueSuffix();
        AuthResponse auth = registrarYLogin(suffix);
        Long deporteId = crearDeporte("Fútbol-" + suffix);

        CompeticionCreateRequest req = CompeticionCreateRequest.builder()
                .nombre("Liga Test " + suffix)
                .deporteId(deporteId)
                .publica(true)
                .inscripcionAbierta(true)
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                url("/competiciones"), HttpMethod.POST,
                new HttpEntity<>(req, authHeaders(auth.getAccessToken())), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void crearCompeticion_sinAuth_devuelve401() {
        Long deporteId = crearDeporte("Fútbol-" + uniqueSuffix());

        CompeticionCreateRequest req = CompeticionCreateRequest.builder()
                .nombre("Liga Fail")
                .deporteId(deporteId)
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/competiciones"), req, String.class);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    // =========================================================
    // Flujo completo: crear competición → equipo → inscribir
    // =========================================================

    @Test
    @SuppressWarnings("unchecked")
    void inscribirEquipo_flujoCompleto_devuelve201() {
        String suffix = uniqueSuffix();
        AuthResponse auth = registrarYLogin(suffix);
        String token = auth.getAccessToken();
        Long userId = auth.getUsuario().getId();
        Long deporteId = crearDeporte("Fútbol-" + suffix);

        // 1. Crear competición
        CompeticionCreateRequest compReq = CompeticionCreateRequest.builder()
                .nombre("Copa " + suffix)
                .deporteId(deporteId)
                .inscripcionAbierta(true)
                .publica(true)
                .build();
        ResponseEntity<Map> compResp = restTemplate.exchange(
                url("/competiciones"), HttpMethod.POST,
                new HttpEntity<>(compReq, authHeaders(token)), Map.class);
        assertThat(compResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long competicionId = ((Number) compResp.getBody().get("id")).longValue();

        // 2. Crear equipo
        EquipoCreateRequest equipoReq = EquipoCreateRequest.builder()
                .nombre("Equipo Alpha " + suffix)
                .build();
        ResponseEntity<Map> equipoResp = restTemplate.exchange(
                url("/equipos"), HttpMethod.POST,
                new HttpEntity<>(equipoReq, authHeaders(token)), Map.class);
        assertThat(equipoResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long equipoId = ((Number) equipoResp.getBody().get("id")).longValue();

        // 3. Inscribir equipo en competición
        ResponseEntity<String> inscribir = restTemplate.exchange(
                url("/competiciones/" + competicionId + "/equipos/" + equipoId
                        + "?usuarioId=" + userId),
                HttpMethod.POST,
                new HttpEntity<>(null, authHeaders(token)),
                String.class);

        assertThat(inscribir.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    // =========================================================
    // Consultar mis competiciones (endpoint protegido)
    // =========================================================

    @Test
    void misCompeticiones_autenticado_devuelve200() {
        String suffix = uniqueSuffix();
        AuthResponse auth = registrarYLogin(suffix);
        String token = auth.getAccessToken();
        Long userId = auth.getUsuario().getId();

        ResponseEntity<String> response = restTemplate.exchange(
                url("/competiciones/mis-competiciones/creador?usuarioId=" + userId),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
