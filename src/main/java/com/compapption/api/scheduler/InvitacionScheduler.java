package com.compapption.api.scheduler;

import com.compapption.api.service.InvitacionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada que gestiona la expiración automática de invitaciones.
 *
 * <p>Se ejecuta diariamente a las 03:00 AM (expresión cron {@code 0 0 3 * * *})
 * y delega en {@link com.compapption.api.service.InvitacionService#marcarExpiradas()}
 * la lógica de negocio para marcar como expiradas todas las invitaciones cuya fecha
 * límite de aceptación ha vencido.
 *
 * <p>Las invitaciones tienen un plazo de aceptación de 7 días desde su creación.
 * Transcurrido ese plazo, pasan al estado {@code EXPIRADA} y ya no pueden ser aceptadas
 * ni rechazadas por el destinatario.
 *
 * <p>Requiere que {@code @EnableScheduling} esté activo en la aplicación (configurado
 * en la clase principal {@code CompeticionesApiApplication}).
 *
 * @author Mario
 */
@Component
public class InvitacionScheduler {

    private final InvitacionService invitacionService;

    public InvitacionScheduler(InvitacionService invitacionService) {
        this.invitacionService = invitacionService;
    }

    /**
     * Marca como expiradas todas las invitaciones cuyo plazo ha vencido.
     *
     * <p>Se ejecuta automáticamente cada día a las 03:00 AM mediante la expresión cron
     * {@code 0 0 3 * * *}. Invoca {@code InvitacionService.marcarExpiradas()}, que
     * actualiza en base de datos el estado de las invitaciones pendientes cuya fecha de
     * expiración es anterior al momento de la ejecución.
     */
    @Scheduled(cron = "0 0 3 * * *") // cada día a las 3:00 AM
    public void marcarInvitacionesExpiradas() {
        invitacionService.marcarExpiradas();
    }
}
