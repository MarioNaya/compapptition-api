package com.compapption.api.it;

import com.compapption.api.dto.auth.AuthResponse;
import com.compapption.api.dto.auth.LoginRequest;
import com.compapption.api.dto.auth.RegistroRequest;
import com.compapption.api.util.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración del flujo de autenticación completo.
 * Cada test usa un suffix UUID único para evitar conflictos de username/email.
 */
class FlujoAuthIT extends BaseIntegrationTest {

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private RegistroRequest buildRegistroRequest(String suffix) {
        return RegistroRequest.builder()
                .username("user_" + suffix)
                .email("user_" + suffix + "@test.com")
                .password("password123")
                .build();
    }

    // =========================================================
    // registro()
    // =========================================================

    @Test
    void registro_datosValidos_devuelve200ConToken() {
        RegistroRequest req = buildRegistroRequest(uniqueSuffix());

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                url("/auth/registro"), req, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getUsuario().getUsername()).isEqualTo(req.getUsername());
    }

    @Test
    void registro_usernameYaUsado_devuelve400() {
        String suffix = uniqueSuffix();
        RegistroRequest req = buildRegistroRequest(suffix);
        restTemplate.postForEntity(url("/auth/registro"), req, String.class);

        // Mismo username, email diferente
        RegistroRequest duplicate = RegistroRequest.builder()
                .username(req.getUsername())
                .email("other_" + suffix + "@test.com")
                .password("password123")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/auth/registro"), duplicate, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void registro_emailYaUsado_devuelve400() {
        String suffix = uniqueSuffix();
        RegistroRequest req = buildRegistroRequest(suffix);
        restTemplate.postForEntity(url("/auth/registro"), req, String.class);

        // Username diferente, mismo email
        RegistroRequest duplicate = RegistroRequest.builder()
                .username("other_" + suffix)
                .email(req.getEmail())
                .password("password123")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/auth/registro"), duplicate, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // =========================================================
    // login()
    // =========================================================

    @Test
    void login_credencialesValidas_devuelve200ConToken() {
        String suffix = uniqueSuffix();
        RegistroRequest registro = buildRegistroRequest(suffix);
        restTemplate.postForEntity(url("/auth/registro"), registro, String.class);

        LoginRequest loginReq = LoginRequest.builder()
                .usernameOrEmail("user_" + suffix)
                .password("password123")
                .build();

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                url("/auth/login"), loginReq, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getUsuario().getUsername()).isEqualTo("user_" + suffix);
    }

    @Test
    void login_passwordIncorrecta_devuelve401() {
        String suffix = uniqueSuffix();
        restTemplate.postForEntity(url("/auth/registro"), buildRegistroRequest(suffix), String.class);

        LoginRequest loginReq = LoginRequest.builder()
                .usernameOrEmail("user_" + suffix)
                .password("WRONG_PASSWORD")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/auth/login"), loginReq, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_usuarioInexistente_devuelve401() {
        LoginRequest loginReq = LoginRequest.builder()
                .usernameOrEmail("nonexistent_user_" + uniqueSuffix())
                .password("password123")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/auth/login"), loginReq, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // =========================================================
    // Endpoints protegidos requieren auth
    // =========================================================

    @Test
    void endpointProtegido_sinToken_devuelve401() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/usuarios"), String.class);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }
}
