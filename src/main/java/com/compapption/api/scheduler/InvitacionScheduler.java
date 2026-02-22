package com.compapption.api.scheduler;

import com.compapption.api.service.InvitacionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InvitacionScheduler {

    private final InvitacionService invitacionService;

    public InvitacionScheduler(InvitacionService invitacionService) {
        this.invitacionService = invitacionService;
    }

    @Scheduled(cron = "0 0 3 * * *") // cada día a las 3:00 AM
    public void marcarInvitacionesExpiradas() {
        invitacionService.marcarExpiradas();
    }
}
