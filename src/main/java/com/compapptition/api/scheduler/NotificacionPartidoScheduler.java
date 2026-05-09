package com.compapptition.api.scheduler;

import com.compapptition.api.service.EventoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada que notifica por email a los jugadores de los partidos
 * próximos (~24h antes del kickoff).
 *
 * <p>Se ejecuta cada hora en punto (expresión cron {@code 0 0 * * * *}) y
 * delega en {@link EventoService#notificarPartidosProximos()}, que busca
 * eventos PROGRAMADOS cuya {@code fechaHora} cae en la ventana
 * {@code [now+23h, now+25h]} y todavía no se han notificado, e invoca
 * el envío de emails para los jugadores activos de ambos equipos.
 *
 * <p>La ventana de 2h da margen de error: si el cron se salta una
 * ejecución, el siguiente tick recoge los eventos pendientes. La
 * idempotencia se garantiza vía el flag {@code notificadoPartido} de
 * la entidad {@code Evento}.
 *
 * <p>Para re-notificación tras un cambio de fecha o lugar el admin
 * de competición dispone del endpoint manual
 * {@code POST /competiciones/{competicionId}/eventos/{id}/notificar-jugadores},
 * que ignora el flag.
 *
 * <p>Requiere que {@code @EnableScheduling} esté activo en la aplicación
 * (configurado en la clase principal {@code ApiApplication}).
 *
 * @author Mario
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionPartidoScheduler {

    private final EventoService eventoService;

    /**
     * Lanza la notificación de partidos próximos. La ejecución es síncrona
     * dentro del servicio pero los emails individuales se encolan en
     * {@code @Async} dentro de {@code EmailService}, por lo que el método
     * vuelve sin esperar a la entrega real de cada correo.
     */
    @Scheduled(cron = "0 0 * * * *") // cada hora en punto
    public void notificarPartidosProximos() {
        try {
            int enviados = eventoService.notificarPartidosProximos();
            if (enviados > 0) {
                log.info("[NotificacionPartido] {} emails encolados en este tick", enviados);
            }
        } catch (Exception e) {
            // Capturamos cualquier excepción aquí para que un fallo no
            // detenga la programación del scheduler. El detalle se loguea
            // y el siguiente tick lo intentará de nuevo gracias a la
            // ventana de 2h y al flag idempotente por evento.
            log.error("[NotificacionPartido] Error en la notificación automática", e);
        }
    }
}
