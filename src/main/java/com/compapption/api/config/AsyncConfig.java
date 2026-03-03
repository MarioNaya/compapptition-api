package com.compapption.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuración de la ejecución asíncrona de métodos en la aplicación.
 *
 * <p>Habilita el soporte de {@code @Async} mediante {@link EnableAsync}, lo que permite
 * que los métodos anotados con {@code @Async} se ejecuten en un hilo separado del pool
 * gestionado por Spring.
 *
 * <p>El caso de uso principal es {@code LogAsyncWriter.escribir()}, que persiste las
 * entradas de auditoría ({@code LogModificacion}) de forma no bloqueante para no
 * penalizar el tiempo de respuesta de los endpoints.
 *
 * <p>Spring utiliza el {@code TaskExecutor} por defecto ({@code SimpleAsyncTaskExecutor})
 * salvo que se declare un bean {@code ThreadPoolTaskExecutor} personalizado.
 *
 * @author Mario
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
