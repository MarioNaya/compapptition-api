package com.compapption.api.util;

import com.compapption.api.service.EmailService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base para tests de repositorio JPA con MySQL real (MySQLTestContainer singleton).
 * @SpringBootTest carga el contexto completo sin servidor HTTP real (webEnvironment=MOCK).
 * @Transactional garantiza rollback automático tras cada test method.
 *
 * No usa @Testcontainers/@Container: el contenedor es un singleton Java estático
 * (MySQLTestContainer) que vive toda la JVM, evitando reinicios entre clases de test
 * que romperían el caché de contextos de Spring Test.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseRepositoryIT {

    @MockitoBean
    protected EmailService emailService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MySQLTestContainer::getJdbcUrl);
        registry.add("spring.datasource.username", MySQLTestContainer::getUsername);
        registry.add("spring.datasource.password", MySQLTestContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // 'update' en lugar de 'create-drop': varios contextos comparten el mismo
        // esquema sin que uno elimine las tablas del otro.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.MySQLDialect");
    }
}
