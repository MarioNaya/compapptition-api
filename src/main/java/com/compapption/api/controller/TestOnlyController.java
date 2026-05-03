package com.compapption.api.controller;

import com.compapption.api.entity.Usuario;
import com.compapption.api.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoints reservados a tests E2E.
 *
 * <p><b>Sólo se cargan en perfil {@code test}</b> ({@code @Profile("test")}).
 * Aun así, cada método verifica explícitamente la propiedad
 * {@code app.test-mode.enabled} como segunda capa de defensa: si por error
 * el perfil test se activase en producción, los endpoints responderían 403.</p>
 *
 * <p>Diseñados para que la suite Playwright pueda dejar la BD limpia entre
 * escenarios sin tocar fixtures externas.</p>
 *
 * @author Mario
 */
@Slf4j
@RestController
@RequestMapping("/test-only")
@Profile("test")
@RequiredArgsConstructor
public class TestOnlyController {

    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    private final UsuarioRepository usuarioRepository;

    @Value("${app.test-mode.enabled:false}")
    private boolean testModeEnabled;

    /**
     * Vacía todas las tablas y crea un usuario admin de sistema canónico
     * para los tests E2E.
     *
     * <p>Credenciales del admin recreado:
     * <ul>
     *   <li>username: {@code admin-e2e}</li>
     *   <li>password: {@code Admin1234}</li>
     *   <li>email: {@code admin-e2e@compapption.test}</li>
     * </ul></p>
     */
    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<Map<String, String>> reset() {
        if (!testModeEnabled) {
            throw new AccessDeniedException("Test mode no habilitado");
        }
        log.warn("=== RESET DB DE TESTS — ESTO SOLO DEBE OCURRIR EN PERFIL test ===");

        // Borrado en orden inverso de dependencias para respetar FKs.
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        for (String tabla : new String[]{
                "estadistica", "evento", "calendario_jornada", "clasificacion",
                "equipo_jugador", "equipo_manager", "usuario_rol_competicion",
                "ticket", "mensaje", "conversacion", "notificacion",
                "invitacion", "solicitud_vinculacion_jugador",
                "equipo", "jugador", "competicion",
                "tipo_estadistica", "deporte",
                "log_modificacion", "usuario",
        }) {
            try {
                em.createNativeQuery("TRUNCATE TABLE " + tabla).executeUpdate();
            } catch (Exception ignored) {
                // tabla puede no existir si el esquema cambia: lo ignoramos en test.
            }
        }
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

        // Re-crea admin canónico.
        Usuario admin = Usuario.builder()
                .username("admin-e2e")
                .email("admin-e2e@compapption.test")
                .password(passwordEncoder.encode("Admin1234"))
                .nombre("Admin")
                .apellidos("E2E")
                .esAdminSistema(true)
                .activo(true)
                .build();
        usuarioRepository.save(admin);

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "adminUsername", "admin-e2e",
                "adminPassword", "Admin1234"
        ));
    }
}
