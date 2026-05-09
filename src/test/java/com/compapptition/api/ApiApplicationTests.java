package com.compapptition.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de carga del contexto Spring. Usa el perfil {@code test} (H2 en memoria)
 * para no consumir cuota de conexiones del MySQL del hosting cuando se ejecuta
 * la suite completa con {@code mvn test}.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
