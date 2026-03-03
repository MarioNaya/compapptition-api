package com.compapption.api.config;

import com.compapption.api.entity.Rol;
import com.compapption.api.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de datos que se ejecuta automáticamente al arrancar la aplicación.
 *
 * <p>Implementa {@link ApplicationRunner} para que Spring Boot lo invoque una vez
 * que el contexto de aplicación está completamente cargado, justo antes de comenzar
 * a aceptar peticiones.
 *
 * <p>Responsabilidades actuales:
 * <ul>
 *   <li>Crear en base de datos los roles del sistema definidos en el enum
 *       {@link com.compapption.api.entity.Rol.RolNombre} si no existen todavía
 *       ({@code ADMIN_COMPETICION}, {@code MANAGER_EQUIPO}, {@code JUGADOR}).</li>
 * </ul>
 *
 * <p>El inicializador es idempotente: comprueba la existencia antes de insertar,
 * por lo que puede ejecutarse en cada arranque sin efectos secundarios.
 *
 * @author Mario
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RolRepository rolRepository;

    /**
     * Punto de entrada del inicializador. Invocado por Spring Boot tras arrancar el contexto.
     *
     * @param args argumentos de la aplicación (no se utilizan).
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
    }

    private void seedRoles() {
        for (Rol.RolNombre rolNombre : Rol.RolNombre.values()) {
            if (!rolRepository.existsByNombre(rolNombre)) {
                rolRepository.save(Rol.builder()
                        .nombre(rolNombre)
                        .descripcion(rolNombre.name())
                        .build());
                log.info("Rol creado: {}", rolNombre);
            }
        }
    }
}
