package com.compapption.api.util;

import com.compapption.api.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Base para tests de integración HTTP con @SpringBootTest(RANDOM_PORT) + MySQL real.
 * Usa MySQLTestContainer singleton: el contenedor arranca una sola vez para toda la suite.
 * EmailService está mockeado para evitar intentos de conexión SMTP.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @MockitoBean
    protected EmailService emailService;

    @LocalServerPort
    protected int port;

    protected RestTemplate restTemplate;

    @BeforeEach
    void initRestTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(HttpStatusCode statusCode) {
                return false; // nunca lanzar excepción — los tests verifican el status ellos mismos
            }
        });
        restTemplate = rt;
    }

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MySQLTestContainer::getJdbcUrl);
        registry.add("spring.datasource.username", MySQLTestContainer::getUsername);
        registry.add("spring.datasource.password", MySQLTestContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.MySQLDialect");
    }
}
