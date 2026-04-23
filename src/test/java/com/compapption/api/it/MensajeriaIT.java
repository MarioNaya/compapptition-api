package com.compapption.api.it;

import com.compapption.api.dto.auth.AuthResponse;
import com.compapption.api.dto.auth.LoginRequest;
import com.compapption.api.dto.auth.RegistroRequest;
import com.compapption.api.request.mensaje.ConversacionStartRequest;
import com.compapption.api.request.mensaje.MensajeCreateRequest;
import com.compapption.api.util.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración end-to-end del flujo básico de mensajería 1-a-1.
 * Cubre: crear conversación → enviar mensaje → listar conversaciones con unreadCount.
 */
class MensajeriaIT extends BaseIntegrationTest {

    private final String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

    private Long aliceId;
    private String aliceToken;
    private Long bobId;
    private String bobToken;

    @BeforeEach
    void setUp() {
        // Registrar alice
        RegistroRequest regAlice = RegistroRequest.builder()
                .username("alice_" + suffix)
                .email("alice_" + suffix + "@test.com")
                .password("password123")
                .build();
        ResponseEntity<AuthResponse> aliceResp = restTemplate.postForEntity(
                url("/auth/registro"), regAlice, AuthResponse.class);
        aliceId = aliceResp.getBody().getUsuario().getId();

        ResponseEntity<AuthResponse> aliceLogin = restTemplate.postForEntity(
                url("/auth/login"),
                LoginRequest.builder()
                        .usernameOrEmail("alice_" + suffix)
                        .password("password123")
                        .build(),
                AuthResponse.class);
        aliceToken = aliceLogin.getBody().getAccessToken();

        // Registrar bob
        RegistroRequest regBob = RegistroRequest.builder()
                .username("bob_" + suffix)
                .email("bob_" + suffix + "@test.com")
                .password("password123")
                .build();
        ResponseEntity<AuthResponse> bobResp = restTemplate.postForEntity(
                url("/auth/registro"), regBob, AuthResponse.class);
        bobId = bobResp.getBody().getUsuario().getId();

        ResponseEntity<AuthResponse> bobLogin = restTemplate.postForEntity(
                url("/auth/login"),
                LoginRequest.builder()
                        .usernameOrEmail("bob_" + suffix)
                        .password("password123")
                        .build(),
                AuthResponse.class);
        bobToken = bobLogin.getBody().getAccessToken();
    }

    private HttpHeaders auth(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void flujoCompleto_crearConversacionEnviarMensajeYLeer() {
        // 1. Alice inicia conversación con Bob
        ConversacionStartRequest start = new ConversacionStartRequest(bobId);
        ResponseEntity<Map> convResp = restTemplate.exchange(
                url("/conversaciones"),
                HttpMethod.POST,
                new HttpEntity<>(start, auth(aliceToken)),
                Map.class);

        assertThat(convResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long conversacionId = ((Number) convResp.getBody().get("id")).longValue();
        assertThat(convResp.getBody().get("otroUsuarioId")).isEqualTo(bobId.intValue());

        // 2. Alice envía un mensaje a Bob
        MensajeCreateRequest msg = new MensajeCreateRequest("Hola Bob!");
        ResponseEntity<Map> msgResp = restTemplate.exchange(
                url("/conversaciones/" + conversacionId + "/mensajes"),
                HttpMethod.POST,
                new HttpEntity<>(msg, auth(aliceToken)),
                Map.class);

        assertThat(msgResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(msgResp.getBody().get("contenido")).isEqualTo("Hola Bob!");
        assertThat(msgResp.getBody().get("autorId")).isEqualTo(aliceId.intValue());

        // 3. Bob lista sus conversaciones — ve 1 mensaje sin leer
        ResponseEntity<List> listResp = restTemplate.exchange(
                url("/conversaciones"),
                HttpMethod.GET,
                new HttpEntity<>(null, auth(bobToken)),
                List.class);

        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResp.getBody()).isNotEmpty();
        @SuppressWarnings("unchecked")
        Map<String, Object> convDeBob = (Map<String, Object>) listResp.getBody().get(0);
        assertThat(convDeBob.get("otroUsuarioId")).isEqualTo(aliceId.intValue());
        assertThat(convDeBob.get("ultimoMensaje")).isEqualTo("Hola Bob!");
        assertThat(((Number) convDeBob.get("unreadCount")).longValue()).isEqualTo(1L);

        // 4. Bob marca como leído — ahora unreadCount=0
        ResponseEntity<Map> leerResp = restTemplate.exchange(
                url("/conversaciones/" + conversacionId + "/leer"),
                HttpMethod.PATCH,
                new HttpEntity<>(null, auth(bobToken)),
                Map.class);

        assertThat(leerResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((Number) leerResp.getBody().get("actualizados")).intValue()).isEqualTo(1);

        // Verificar que ahora ya no hay mensajes sin leer
        ResponseEntity<List> listResp2 = restTemplate.exchange(
                url("/conversaciones"),
                HttpMethod.GET,
                new HttpEntity<>(null, auth(bobToken)),
                List.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> convBob2 = (Map<String, Object>) listResp2.getBody().get(0);
        assertThat(((Number) convBob2.get("unreadCount")).longValue()).isEqualTo(0L);
    }
}
