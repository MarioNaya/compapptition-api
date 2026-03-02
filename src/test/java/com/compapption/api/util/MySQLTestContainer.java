package com.compapption.api.util;

import org.testcontainers.containers.MySQLContainer;

/**
 * Singleton MySQL container compartido por toda la suite de integración.
 *
 * Por qué singleton: Testcontainers arranca un contenedor por clase de test si se usa
 * @Container static en la base. Al cambiar de clase, el contenedor anterior se para y
 * el nuevo obtiene un puerto diferente. Spring Test cachea el contexto apuntando al
 * puerto antiguo → CannotCreateTransaction.
 *
 * Con un singleton estático Java (inicializado en bloque static), el contenedor arranca
 * UNA VEZ cuando la JVM carga la clase y vive hasta que la JVM termina. La URL/puerto
 * es siempre la misma → Spring puede reutilizar el contexto cacheado sin problemas.
 */
public final class MySQLTestContainer {

    private static final MySQLContainer<?> CONTAINER;

    static {
        CONTAINER = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("compapption_test")
                .withUsername("test")
                .withPassword("test");
        CONTAINER.start();
    }

    public static String getJdbcUrl()  { return CONTAINER.getJdbcUrl(); }
    public static String getUsername() { return CONTAINER.getUsername(); }
    public static String getPassword() { return CONTAINER.getPassword(); }

    private MySQLTestContainer() {}
}
