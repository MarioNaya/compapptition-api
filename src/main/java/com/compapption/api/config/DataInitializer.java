package com.compapption.api.config;

import com.compapption.api.entity.Rol;
import com.compapption.api.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RolRepository rolRepository;

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
